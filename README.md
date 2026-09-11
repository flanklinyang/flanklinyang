# 论文查重（Java）

本作业使用 Java 8 兼容语法实现，可以在 Java 8 及以上运行时中执行。入口文件为 `papercheck.Main`。程序只读取命令行传入的原文和待检查文件，并把结果写入命令行指定的答案文件。

## 运行方式

```text
java -jar main.jar [原文文件] [抄袭版论文文件] [答案文件]
```

例如：

```text
java -jar main.jar D:\tests\orig.txt D:\tests\orig_add.txt D:\tests\ans.txt
```

答案文件内容为 `0.00` 到 `1.00` 之间的浮点数，保留两位小数。

## 算法说明

程序将文本统一为 Unicode 码点序列，不依赖外部中文分词库：

1. 顺序相似度使用最长公共子序列（LCS），结果为 `2 * LCS / (len(original) + len(copied))`，用于检测增、删、改造成的局部差异。超过 2500 万个 LCS 计算单元的输入自动跳过该步，避免超时。
2. 无序相似度使用 3 到 6 字符长度的 n-gram Dice 系数，用于检测段落后被重新排列的情况。
3. 最终结果取两种算法的较大值。shingle 比较会忽略大小写、全角 ASCII、标点和空白；LCS 比较会保留标点作为顺序锚点，避免短样例对局部改写过度敏感。

## 构建与测试

```text
mvn clean package
java -jar target/main.jar sample/orig.txt sample/copied.txt sample/ans.txt
```

单元测试位于 `src/test/java/papercheck/`，覆盖核心算法、文件读写、命令行参数和异常路径。
