> 算法二（SPWSE-2）的文档有一些错误

加密时 `E[i]`的计算，底数为 g，而非 g1

陷门生成时

1. `K[i]`的计算，底数应为 g
2. T4 和 T5 的计算，指数部分应多乘上随机数 s

线性验证时，公式有误，分子部分应为
$$
\prod_{i=1}^{2n}e(E[i]\cdot P[i],K[i])
$$
详见👇

[SPWSE-1/2 算法仿真 | Northboat Dock](https://northboat.github.io/docs/sec/crypto/se/se-spwse.html)
