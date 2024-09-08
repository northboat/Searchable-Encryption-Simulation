---
title: BM25 可搜索加密仿真
date: 2024-7-23
tags:
  - SearchableEncryption
categories:
  - sec
---

## JPBC 基础

外部 jar 包导入`jpbc-api-2.0.0.jar`以及`jpbc-plaf-2.0.0.jar`，在项目根目录下复制进加密参数配置`a.properties`

### 双线性验证

验证公式
$$
e(u^a, v^b)=e(u, v)^{ab}
$$

```java
Pairing bp = PairingFactory.getPairing("a.properties");
Field G1 = bp.getG1();
Field G2 = bp.getG2();
Field Zr = bp.getZr();

Element u = G1.newRandomElement().getImmutable();
Element v = G2.newRandomElement().getImmutable();
Element a = Zr.newRandomElement().getImmutable();
Element b = Zr.newRandomElement().getImmutable();
System.out.println(u);
System.out.println(v);
System.out.println(a);
System.out.println(b);

// 计算等式左半部分 e(u^a, v^b)
Element ua = u.powZn(a);
Element vb = v.powZn(b);
Element left = bp.pairing(ua,vb);

// 计算等式右半部分 e(u, v)^ab
Element euv = bp.pairing(u,v).getImmutable();
Element ab = a.mul(b);
Element right = euv.powZn(ab);

if (left.isEqual(right)) {
    System.out.println("Yes");
} else {
    System.out.println("No");
}
```

### 哈希嵌入

将哈希值`byte[]`嵌入到群 G1 中

```java
try{
    byte[] md5 = DigestUtils.md5Digest(left.toBytes());
    Element hash = G1.newElement().setFromHash(md5, 0, md5.length);
    System.out.println(hash);
}catch (Exception e){
    e.printStackTrace();
}
```

其中 MD5 哈希调用 spring 内置的工具类

```java
import org.springframework.util.DigestUtils;
```

**但注意，对于 Type A1 来说，这个代码无法指定哈希到指定子群 G 中。解决方法是将`byte[]`先哈希到Z群，然后利用`G, GT`的生成元计算幂指数，从而达到哈希到`G, GT`上的效果，正确的代码如下**

```java
public static Element hash(byte[] bytes){
    Element hash = null;
    try{
        byte[] md5 = DigestUtils.md5Digest(bytes);
        // 映射到 Zr 整数群
        Element x = Zr.newElement().setFromHash(md5, 0, md5.length);
        // 通过 G1 上的生成元 g 幂运算映射到 G1 群上
        hash = g.powZn(x).getImmutable();
    }catch (Exception e){
        e.printStackTrace();
    }
    return hash;
}
```

### 双线性群的运算

乘方、乘、加法以及配对运算

```java
Element g = G1.newRandomElement();
Element h = G1.newRandomElement();
Element x = Zr.newRandomElement();

Element g1 = g.add(h);
Element g2 = g.mul(h);
Element g3 = g.powZn(x);
Element g4 = bp.pairing(g, h);
```

在进行幂运算时，指数只能为群 Zr 中的整数元素

并且注意，这里在直接运算时，会改变原有的元素 g，如 g 在`g1 = g.add(h)`后，g 本身也会变成二者之和

解决这个问题有两种办法

1. 在定义 g 时取`getImmutable()`，令其不可变

   ```java
   Element g = G1.newRandomElement().getImmutable();
   
   Element x = Zr.newRandomElement();
   Element g5 = g.powZn(x);
   ```

2. 在计算时取`duplicate()`，复制一份进行计算而不影响其自身

   ```java
   Element g = G1.newRandomElement();
   Element x = Zr.newRandomElement();
   
   Element g6 = g.duplicate().powZn(x)
   ```

一定要注意，避免产生一些难以寻找的错误

## 初始化群及密钥

初始化两个循环群，对应的整数群，配对函数，以及四份密钥对

```java
public class SEUtil {
    // 系统初始化

    //循环群 G1,GT, 整数群 Zr
    private static final Field G1, GT, Zr;
    // 两个生成元
    private static final Element g, h;
    private static final Pairing bp;
    // 全局变量初始化
    static{
        bp = PairingFactory.getPairing("a.properties");
        G1 = bp.getG1();
        GT = bp.getGT();
        Zr = bp.getZr();
        g = G1.newRandomElement().getImmutable();
        h = G1.newRandomElement().getImmutable();
    }

    // 密钥生成中心密钥对kgc, 搜索服务器密钥对svr, 数据所有者密钥对co, 数据用户密钥对tu
    private static KeyPair kgc, svr, co, tu;
    // 密钥对初始化
    public static void systemKeyInit(){
        Element x = Zr.newRandomElement().getImmutable();
        Element t = Zr.newRandomElement().getImmutable();
        Element s = Zr.newRandomElement().getImmutable();
        Element r = Zr.newRandomElement().getImmutable();
        

        kgc = new KeyPair(x, g.powZn(x).getImmutable());
        svr = new KeyPair(t, g.powZn(t).getImmutable());
        co = new KeyPair(s, g.powZn(s).getImmutable());
        tu = new KeyPair(r, g.powZn(r).getImmutable());
    }
}
```

## 身份双向认证

> 基于双线性验证
>
> 采用对方公钥、自身私钥加密，对方利用自身私钥和你的公钥进行认证，反之同理

使用同一个整数 x，计算参与认证的双方的密钥对
$$
sk_i = H(ID_i)^x\quad pk_i = H(ID_i)
$$
H 为哈希函数，ID 为用户 ID，是一个在整数群 Zr 中的元素

- 注意这里哈希函数的处理为上文`哈希嵌入`中的方式：即先将哈希值嵌入 Zr 群，再通过生成元幂运算嵌入 G1 群

计算双线性配对值 k
$$
k = e(sk_{ao},pk_{au})=e(sk_{au},pk_{ao})
$$

```java
// 数据私有者和数据用户的 ID
private static Element id_o, id_u, k;
// 用于身份验证（数据私有者和数据用户之间）的两个密钥对
private static KeyPair ao, au;
public static void authKeyInit(){
    id_o = Zr.newRandomElement().getImmutable();
    id_u = Zr.newRandomElement().getImmutable();
    Element x = Zr.newRandomElement();
    ao = new KeyPair(hash(id_o.toBytes()).powZn(x).getImmutable(), hash(id_o.toBytes()));
    au = new KeyPair(hash(id_u.toBytes()).powZn(x).getImmutable(), hash(id_u.toBytes()));

    k = bp.pairing(ao.getSk(), au.getPk()).getImmutable();
}
```

计算 hash
$$
hash = H(pk_{ao},pk_{au},k)
$$
这里对于多个元素进行哈希值得计算，采用的方式是将三个比特数组参数前后拼接然后通过上述的哈希嵌入得到群 G1 中的元素

```java
// 连接三个比特数组
public static byte[] joinByteArray(byte[] byte1, byte[] byte2, byte[] byte3) {
    return ByteBuffer.allocate(byte1.length + byte2.length + byte3.length)
        .put(byte1)
        .put(byte2)
        .put(byte3)
        .array();
}
```

计算 CID，这里的`sk_{co}`是第一步中系统生成的数据所有者的私钥
$$
CID = hash^{sk_{co}}\cdot h
$$
计算 TID，同样的这里的`sk_{tu}`也是第一步中数据用户的私钥，`pk_{co}`是第一步中数据所有者的公钥
$$
TID = e(hash^{sk_{tu}},pk_{co})
$$

```java
// 获取 CID
public static Element getCID(){
    Element k1 = bp.pairing(ao.getSk(), au.getPk()).getImmutable();
    // 注意这里的命名要和生成元 h 区分开
    Element h1 = hash(joinByteArray(ao.getPk().toBytes(), au.getPk().toBytes(), k1.toBytes())).getImmutable();
    return h1.powZn(co.getSk()).mul(h).getImmutable();
}

// 获取 TID
public static Element getTID(){
    Element k2 = bp.pairing(au.getSk(), ao.getPk()).getImmutable();
    Element h2 = hash(joinByteArray(ao.getPk().toBytes(), au.getPk().toBytes(), k2.toBytes())).getImmutable();
    return bp.pairing(h2.powZn(tu.getSk()), co.getPk()).getImmutable();
}
```

双线性验证，若相等则认证通过
$$
e(CID,pk_{tu})==TID\cdot e(pk_{tu},h)
$$

```java
public static boolean auth(Element CID, Element TID){
    Element left = bp.pairing(CID, tu.getPk());
    Element right = TID.mul(bp.pairing(tu.getPk(), h));
    return left.isEqual(right);
}
```

## 可搜索加密

> 采用 BM25 / BM25L 算法实现所谓可搜索加密，思路如下

### 索引构建

首先，我们有一个文档集（可以视作`List<String>`），我们需要提取所有文档的关键词，用一个`Set`存储（去重），同时已知所有文档对应的文档名，即文档 ID

以关键词为行、ID 为列构造这样一个矩阵

| 关键词 \ ID | ID1  | ID2  |
| ----------- | ---- | ---- |
| **W1**      | R11  | R12  |
| **W2**      | R21  | R22  |
| **W3**      | R31  | R32  |

其中 R 为 BM25 公式中的一个子式（我们称之为关键词对于单个文档的相关分数）
$$
BM25(q,d_j) = \sum_{i=1}^nR_{ij}
$$
其中 Rij 为
$$
R_{ij} = IDF(q_i)\,\frac{f(q_i,d_j)\times(k_1+1)}{f(q_i,d_j)+k_1\times(1-b+\frac{|d_j|}{d_{avg}})}\,\frac{f(q_i,d_j)\times(k_3+1)}{k_3+f(q_i,d_j)}
$$
显然，这个 BM25 公式求出的是某一次查询 q 对于文档 dj 的得分，我们在这个索引矩阵中需要存储的值，即为这里的 R 值，为什么要这么存呢？

试想，我们若得到一次查询 q 对于关键词的匹配情况，比如本次查询中有第 7 位和第 14 位的关键词，那么对于第 j 个文档而言，我们只需要取出第 j 列的第 7、14 行的 R 值并相加，就可以得到 q 在文档 dj 的得分

这个得分会直接说明本次查询对文档的匹配程度，从而实现一次搜索

从数学上，我们将 q 构造为一个由 0/1 组成的横向量，令其与矩阵的第 j 列做内积，即可得到这个查询的相关性评分

这个公式的详细解释如下

- d 是文档，q 是查询

- qi 是查询中的第 i 个词

- f(qi, d) 是词 qi 在文档 d 中出现频率

- |d| 是文档 d 的长度（词的总数）

- d_avg 是文档集的平均文档长度

- k1、b、k3 是调节参数，通常 k1 = 1.2, k3 ∈ (1.2, 2), b = 0.75

- IDF(qi) 是 qi 的逆文档频率，表示词的重要性，公式如下
  $$
  IDF(q_i) = log\frac{N-n(q_i)+0.5}{n(q_i)+0.5}
  $$
  其中 N 为文档数，n(qi) 表示包含词 qi 的文档数

注意，当一个文档的关键词个数超过 400 时，判定为长文档，需要采用 BM25L 算法，区别仅在于对每个出现频率 f，变为
$$
f(q_i,d)\Rightarrow(f(q_i,d)+a)\quad a\geq0
$$

接下来从代码层面分解上述操作

#### 文档读入

首先是读入文档，通过维护的一个文档名的序列，从本地读入文件字符串

```java
private static List<String> names = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5"));
```

读入的文件用 Map 存放

```java
private static Map<String, String> docs;
```

同时记录文档 ID 以及初始化各 ID 在索引矩阵 matrix 中的下标（列坐标）

```java
Map<String, Integer> id = new HashMap<>();
```

读入文档

```java
int count = 0;
for(String name: names){
    // 从文件中读入文档
    docs.put(name, IOUtil.readDocs(name));
    // 记录文档并且规定其在矩阵 matrix 中对应的下标
    id.put(name, count++);
}
```

I/O 方法如下

```java
public class IOUtil {
    public static String readDocs(String path) {
        // 如何相对路径读取捏
        File file = new File("E:\\File\\XDU\\项目\\可搜索加密算法仿真\\se-emulation\\src\\main\\resources\\"+path);
        String docs = null;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            // 接收文件内容 stringBuffer线程安全
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            docs = stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 关闭流
        return docs;
    }
}
```

同时记录文档个数 N，用一个向量 d 记录各个文档的词总数（通过空格分割），并且计算文档集的平均次数

```java
int N = id.size();

int[] d = new int[id.size()];
for(String name: id.keySet()){
    // 遍历文档 ID，从 Map id 里拿 id 对应的下标
    int index = id.get(name);
    // 记录各个文档的词汇总数，index 为 id 表中的值
    // 取的时候一定要注意，先从 id 表中通过文档名取出下标，然后再通过下标去操作 d 数组或 matrix 数组
    d[index] = docs.get(name).split(" ").length;
}
// 求文档包含的平均词数
int avg = Arrays.stream(d).sum() / N;
```

#### 关键词提取

通过下述包进行分词

```xml
<dependency>
    <groupId>com.hankcs</groupId>
    <artifactId>hanlp</artifactId>
    <version>portable-1.7.6</version>
</dependency>
```

具体代码见 DocsUtil.java 提供的 getKeyword() 方法

对刚刚读入的文档们进行分词，将提取的关键词存入 Map 中，第一是为了去重，第二，和 ID 一样，用 Map 的值存储其在 matrix 中的索引下标，即对应的行

```java
count = 0;
// 从 docs 表中取出文档内容
// 通过分词算法提取关键词并存入 keyword 表，值为其在二维数组 matrix 中的下标
for(String content: docs.values()){
    List<String> l = DocsUtil.getKeyword("", content);
    for(String s: l){
        if(!keyword.containsKey(s)){
            // 放入关键词，同时规定关键词在矩阵 matrix 中的下标
            keyword.put(s, count++);
            // 统计关键词在文档中出现次数
            n.put(s, 1);
        } else {
            // 更新关键词在各文档中的出现次数
            n.put(s, n.get(s)+1);
        }
    }
}
```

其中 Map<String, Integer> n 是在记录关键词被文档包含的次数，用于后续 BM25 计算

#### 构建 matrix

至此行列齐全，公式中的 n、d 也都准备好，可以计算构建索引矩阵

```java
matrix = new String[keyword.size()][id.size()];
for(String q: keyword.keySet()){
    // matrix 行
    int i = keyword.get(q);
    for(String s: id.keySet()){
        // matrix 列
        int j = id.get(s);
        // 计算 R
        double idf = Math.log((N + 1) / (n.get(q) + 0.5));
        // 计算第 i 个关键词在第 j 个文档中的出现频率
        int f = DocsUtil.getMatch(q, docs.get(s));
        // 若为长文档，需要额外处理 f，加一个常数 a=1
        if(d[j] > 400){
            f += a;
        }
        double R = (idf * (f * (k1+1) / (f + k1 * (1 - b + b * d[j] / avg))) * (f * (k3 + 1) / (k3 + f)));
        // 加密存储
        String encode = VFEPlusUtil.encode((int)R);
        matrix[i][j] = encode;
    }
}
```

这里在对评分 R 进行存储时，采用 VFE-Plus 算法进行加密，将整型转换为一个八进制的字符串进行存储，至此索引矩阵构建完毕

### 序列加密

这里主要是关键词序列的加密以及文档 ID 序列的加密

- 文档 ID 采用对称加密算法 AES
- 关键词序列采用公钥加密

对文档 ID 进行 AES 加密

```java
// 初始化分组加密参数
secretKey = AESUtil.generateKey();
iv = AESUtil.genIV();
ID = new String[id.size()];
for(String s: id.keySet()){
    String c = AESUtil.encrypt(s, secretKey, iv);
    int index = id.get(s);
    ID[index] = c;
}
System.out.println("文档 ID 序列（AES 加密后）: " + Arrays.toString(ID));
```

对关键词进行公钥加密，公式如下
$$
C_w=pk_{svr}^{sk_{co}\cdot h_2(h_1(k,w),pk_{co})}
$$
其中，w 是关键词，`pk_{svr}`是系统初始化时搜索服务器的公钥，`sk_{co}`和`pk_{co}`分别是用户的私钥和公钥，h2 和 h1 是两个哈希函数，一定要注意在经过 h2 的嵌入后，哈希值需要映射到整数群 Zr 上（才能进行幂运算），和之前的哈希嵌入（映射到 G1 上）不一样

```java
KEYWORD = new Element[keyword.size()];
// 在 keyword 表中加密关键词，得到最终加密后的关键词表 KEYWORD
for(String w: keyword.keySet()){
    Element h1 = hashZ(ByteUtil.joinByteArray(k.toBytes(), w.getBytes()));
    Element h2 = hashZ(ByteUtil.joinByteArray(h1.toBytes(), co.getPk().toBytes()));
    Element x = co.getSk().mul(h2).getImmutable();
    Element c = svr.getPk().powZn(x).getImmutable();
    int index = keyword.get(w);
    KEYWORD[index] = c;
}
```

这里为了简便 h1 = h2，至此加密完毕

### 加密搜索

#### 关键词匹配

陷门生成：计算所要搜索的关键词的哈希值，并嵌入到循环群 Zr 中，用于后续的公钥加密，对加密后的关键词序列进行匹配

还记得刚刚的关键词加密吗，采用的公式为
$$
C_w=pk_{svr}^{sk_{co}\cdot h_2(h_1(k,w),pk_{co})}
$$
所以我们在进行关键词匹配之前，同样要经过这一计算，例如，现在要匹配关键词 w'，那么受限我们计算陷门
$$
T_{w'}=h_1(k,w')
$$

```java
public static Element getT(String w){
    return hashZ(ByteUtil.joinByteArray(k.toBytes(), w.getBytes()));
}
```

再以陷门为参数计算
$$
C_{w'}=pk_{svr}^{sk_{co}\cdot h_2(T_{w'},pk_{co})}
$$
在刚刚生成的 KEYWORD 数组中，采取遍历的方式去匹配这一加密值，若一样则匹配成功，返回其在 KEYWORD 中的下标 i

```java
public static int find(String str){
    // 计算陷门
    Element t = getT(str);
    // 计算密文
    Element word = svr.getPk().powZn(co.getSk().mul(hashZ(ByteUtil.joinByteArray(t.toBytes(), co.getPk().toBytes()))));
    // 在关键词序列中匹配密文
    for(int i = 0; i < KEYWORD.length; i++){
        Element w = KEYWORD[i];
        if(w.isEqual(word)){
            return i;
        }
    }
    return -1;
}
```

#### 查询向量生成

在匹配关键词的基础上，对一连串的字符串查询进行匹配，返回一个长度为 KEYWORD.length 的向量，匹配成功的位置置 1，否则为 0

```java
// 获取查询向量，在 words 与 KEYWORD 匹配的地方置 1，否则置 0
public static List<Integer> getQuery(List<String> words){
    List<Integer> query = new ArrayList<>();
    // 初始化查询向量
    for(int i = 0; i < KEYWORD.length; i++){
        query.add(0);
    }
    // 匹配成功置 1
    for(String w: words){
        int index = find(w);
        if(index >= 0){
            query.set(index, 1);
        }
    }
    return query;
}
```

#### 相关性评分计算

将查询向量与索引矩阵做内积，即可得到各个文档关于本次查询的相关性评分

```java
// 获取各个文档的 BM25 分数，通过查询向量和索引矩阵的每列（代表每个文档）做内积得到
public static List<Integer> getBM25(List<String> words){
    System.out.println("查询包含的关键词: " + words);
    // 获取查询向量，长度为 KEYWORD.length
    List<Integer> query = getQuery(words);
    System.out.println("对应的查询向量: " + query);
    // 每个文档的 BM25 分数
    // 初始化相关性评分
    List<Integer> bm25 = new ArrayList<>();
    for(int j = 0; j < ID.length; j++){
        int grade = 0;
        for (int i = 0; i < KEYWORD.length; i++) {
            int decode = VFEPlusUtil.decode(matrix[i][j]);
            grade += decode * query.get(i);
        }
        bm25.add(grade);
    }
    return bm25;
}
```

## 测试

### 功能测试

整个初始化、认证、可搜索加密过程功能测试，涉及五个文档

```java
public static void main(String[] args) {
    systemInit();
    authTest();
    try{
        matrixInit();
    }catch (Exception e){
        e.printStackTrace();
    }
    List<String> words = new ArrayList<>();
    words.add("K"); words.add("Mon"); words.add("May"); words.add("Phillip"); words.add("Belden"); words.add("PDT"); words.add("From"); words.add("phillip");
    
    System.out.println("BM25 分数: " + getBM25(words));

}
```

输出结果

```bash
BM25 分数: [4, 1, 0, 0, 0]
```

### 性能测试

对搜索过程测速，增加到 20 个文档

```java
public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
    systemInit();
    authTest();
    try{
        matrixInit();
    }catch (Exception e){
        e.printStackTrace();
    }
    List<String> words = new ArrayList<>();
    words.add("K"); words.add("Mon"); words.add("May"); words.add("Phillip"); words.add("Belden"); words.add("PDT"); words.add("From"); words.add("phillip");

    //获取当前时间为开始时间，转换为long型
    long startTime = fromDateStringToLong(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));

    System.out.println("搜索结果: " + search(words));

    long stopTime = fromDateStringToLong(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
    long timeSpan = stopTime - startTime;
    System.out.println("搜索时间: " + timeSpan + "ms");
}
```

测试结果

```bash
搜索结果: [test, hello, 6, 8, 15, 16, 17, 18, 19]
搜索时间: 99ms
```

