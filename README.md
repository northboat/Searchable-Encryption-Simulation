# Searchable Encryption Simulation
可搜索加密仿真，基于 JPBC 对一些可搜索加密算法进行复现，并测试其关键步骤耗时

- BM25：包括群上的公钥加密、双线性认证、AES 加密、关键词提取、VFE-Plus 编码、BM25/BM25L 加密索引构建（支持范围查询）
- SPWSE：SPWSE-1 和 SPWSE-2
- BP：一系列基于双线性配对的可搜索加密算法
  1. PAUKS √
  2. SA-PAEKS √
  3. dIBAEKS √
  4. ~~DuMSE~~
  5. pMatch √
  6. CR-IMA √
  7. TuCR √
  8. Tu2CKS √
- IPFE：基于属性加密的前缀四叉树构建
