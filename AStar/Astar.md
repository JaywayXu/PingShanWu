#  一种单目标A*算法设计与实现
作者：吴屏珊
* 最近在学习简单的单目标A\*算法，其中在CSDN上阅读到的一篇博文给了我很大启发，于是在该博文的基础上，笔者记录了一点自己对于A\*算法的体会和感悟。
* [原文链接](https://blog.csdn.net/qq_45978890/article/details/115802114)

## 目录
[TOC]
<br>
## 1. A*算法简单介绍
### 1.1 A*算法的基本要素
<div style="text-align: center;">
    <img src="https://i-blog.csdnimg.cn/direct/145cf46e5f0848ed9ece63309042cea8.png" alt="Image" />
</div>
1、图的说明：有一个8*8的地图，其中绿色的点为起点，红色的点为终点，黑色的点为障碍点（即不可达点），当然边界也是不可达点。

2、将要实现的目标：从起点走到终点，其中要求所走距离最短并且要绕开所有不可达点。
3、移动方向：每个点只有上，下，左，右四个方向可以行进。

### 1.2 A*算法的中心思想
* 我们首先思考一个问题作为引入：既然每一个点有四种移动方向，那如何判断该点下一步的移动方向呢，如何选出在该点下一步即将到达的所有点中选出最好的点呢？
（1）这里我们需要用到一个公式：**F=G+H**；其中F称为代价，G为从起点到该点已走过的距离，H为从该点到终点将走的曼哈顿距离。
（2）曼哈顿距离：在地图上我们**忽略所有障碍点（不可达点）**，两点的横坐标之差的绝对值与纵坐标之差的绝对值之和即为两点之间的曼哈顿距离，数学公式表示如下：${|x_1-x_2|+|y_1-y_2|}$ ；代码表示如下：```Math.abs(x1-x2)+Math.abs(y1-y2)```
（3）选取移动方向的依据：**F**，每次移动前，计算下一步可到达点的 F 值，然后对所有可到达点（不限于下一步）的 F 值进行比较，优先选取移动代价最小即 F 值最小的点。
### 1.3 A*算法所需数据结构
#### 1.3.1 定义点Node的结构
1、点的坐标（x，y）
2、计算点的代价时所需数值：F，G，H
3、父节点：Father；父节点记录的是该节点的上一个节点（即该节点是由哪个节点遍历到的）。
&emsp; 父节点的作用：A*算法从起点开始寻路，当找到终点的时候代表最短路径已经找到，这时我们可以利用终点结构的Father来回溯到起点（起点的Father为NULL），从而找出并且输出这条最短路径。
#### 1.3.2 记录点状态的几个表
1、优先队列Open表：记录由该点下一步可以到达的所有点，并且每次将F值最小的点放在队首。
2、Close表：记录所有已经走过的点。
3、Exist表：记录所有遍历过的点（即在Open与Close中出现过的点）。
4、Close表与Exist表的区别：Close中存储的点是已走过的点，Exist表中存储的点是已遍历过，但不一定走过的点。
## 2. A\*算法步骤演示
* **Open表不为空且未找到终点时一直进行循环**
### 2.1 第一轮操作
* 首先我们将起点放入Open表中<br>
![alt text](https://i-blog.csdnimg.cn/direct/a0f8214487e1487c98934ff331907c94.png)
![alt text](https://i-blog.csdnimg.cn/direct/75b05141eb0f46c6960f26faa2a545f4.png)
* 在Open表中找到当前F值最小的结点A，并将该点移出Open表，加入到Close表中。
* 遍历该点A四周所有可到达节点，如果这些节点不包含在Exist表中（即未出现过），则计算它们的F值，并且根据F值大小顺序加入到Open表中。<br>
![alt text](https://i-blog.csdnimg.cn/direct/6a79ac9eea6f402e80f22df954849a38.png)![alt text](https://i-blog.csdnimg.cn/direct/230f3d0092c34ba58b5cc4d0c0beaba2.png)
* 记录这些点的父节点为该点A。<br>
![alt text](https://i-blog.csdnimg.cn/direct/8aa1f4ce7a464375b449559ae7fb66b1.png)
### 2.2 第二轮操作
* 在第一轮操作结束后的Open表中，将队首节点（即F值最小的节点）移出Open表，加入到Close表中。
* 遍历该点B四周所有可到达节点，如果这些节点不包含在Exist表中（即未出现过），则计算它们的F值，并且根据F值大小顺序加入到Open表中。
* （3,3）节点为障碍物，（3,1）节点已被Exist表包含，所以两个点都不加入到Open表中。<br>
![alt text](https://i-blog.csdnimg.cn/direct/f5ee5022ee0c449898504572e4a6b8ec.png)![alt text](https://i-blog.csdnimg.cn/direct/f23a82f07f694f6ea17e01ffc11bb0ce.png)
* 记录新加入这些点的父节点为该点B。<br>
![alt text](https://i-blog.csdnimg.cn/direct/ff26b6f7fe2c4a9db78c75947b433bac.png)
## 3. A*算法实现代码
### 3.1 定义Node类
* init_Node(Father,end)：传入父节点和终点，并计算两者间的曼哈顿距离，最终根据公式算出该点的F值。
* compareTo(Node)：用于比较出F值最小的点
```java
class Node implements Comparable<Node> {
    public int x;  //x坐标
    public int y;  //y坐标
    public int F;  //F属性
    public int G;  //G属性
    public int H;  //H属性
    public Node Father;    //此结点的上一个结点
    //获取当前结点的坐标
    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }
    //通过结点的坐标可以得到F， G， H三个属性
    //需要传入这个节点的上一个节点和最终的结点
    public void init_node(Node father, Node end) {//father是父节点
        this.Father = father;
        if (this.Father != null) {
            this.G = father.G + 1;
        } else { //父节点为空代表它是第一个结点
            this.G = 0;
        }
        //计算通过现在的结点的位置和最终结点的位置计算H值
        this.H = Math.abs(this.x - end.x) + Math.abs(this.y - end.y);
        this.F = this.G + this.H;
    }
    // 用来进行和其他的Node类进行比较
    @Override
    public int compareTo(Node o) {
        return Integer.compare(this.F, o.F);
    }
}
```
### 3.2 Solution方法
* is_exist方法：判断是否在Exist表中出现过。
```java
public boolean is_exist(Node node)
    {
        for (Node exist_node : Exist) {
            if (node.x == exist_node.x && node.y == exist_node.y) {
                return true;
            }
        }
        return false;
    }
```
* is_valid方法：判断是否合法（边界，不可达点，已存在于Exist表中都为不合法）
```java
public boolean is_valid(int x, int y) {
        // 如果结点的位置是-1，则不合法
        if (map[x][y] == -1) return false;
        for (Node node : Exist) {
            //如果结点出现过，不合法
//            if (node.x == x && node.y == y) {
//                return false;
//            }
            if (is_exist(new Node(x, y))) {
                return false;
            }
        }
        //以上情况都没有则合法
        return true;
    }
```
* extend_current_node方法：遍历该点的上下左右四个方向，并判断这些点是否合法。
```java
public ArrayList<Node> extend_current_node(Node current_node) {
        int x = current_node.x;
        int y = current_node.y;
        ArrayList<Node> neighbour_node = new ArrayList<Node>();
        if (is_valid(x + 1, y))
        {
            Node node = new Node(x + 1, y);
            neighbour_node.add(node);
        }
        if (is_valid(x - 1, y))
        {
            Node node = new Node(x -1, y);
            neighbour_node.add(node);
        }
        if (is_valid(x, y + 1))
        {
            Node node = new Node(x, y + 1);
            neighbour_node.add(node);
        }
        if (is_valid(x, y - 1))
        {
            Node node = new Node(x, y - 1);
            neighbour_node.add(node);
        }
        return neighbour_node;
    }
``` 
* astarSearch方法：A*算法具体实现方法。
```java
public Node astarSearch(Node start, Node end) {
        //把第一个开始的结点加入到Open表中
        this.Open.add(start);
        //把出现过的结点加入到Exist表中
        this.Exist.add(start);
        //主循环
        while (Open.size() > 0) {
            //取优先队列顶部元素并且把这个元素从Open表中删除
            Node current_node = Open.poll();
            //将这个结点加入到Close表中
            Close.add(current_node);
            //对当前结点进行扩展，得到一个四周结点的数组
            ArrayList<Node> neighbour_node = extend_current_node(current_node);
            //对这个结点遍历，看是否有目标结点出现
            //没有出现目标结点再看是否出现过
            for (Node node : neighbour_node) {
                if (node.x == end.x && node.y == end.y) {//找到目标结点就返回
                    node.init_node(current_node,end);
                    return node;//返回的是终止结点
                }
                if (!is_exist(node)) {  //没出现过的结点加入到Open表中并且设置父节点
                    node.init_node(current_node, end);
                    Open.add(node);
                    Exist.add(node);
                }
            }
        }
        //如果遍历完所有出现的结点都没有找到最终的结点，返回null
        return null;
    }
```
### 3.3 main方法
* 绘制8*8地图，并设置好起点和终点（原文直接在地图中进行起点终点的设置，修改起点和终点时相对麻烦，我在这里对原文进行了改进，在代码中设置起点终点，测试不同种情况时相对便捷）。
```java
int[][] map = {
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
                {-1,  0,  0,  0,  0,  0,  0,  0,  0, -1},
                {-1,  0,  0,  0,  0, -1,  0,  0,  0, -1},
                {-1,  0,  0,  0, -1,  0,  0,  0,  0, -1},
                {-1,  0,  0,  0, -1,  0,  0,  0,  0, -1},
                {-1,  0,  0,  0,  0, -1,  0,  0,  0, -1},
                {-1,  0,  0,  0, -1,  0,  0,  0,  0, -1},
                {-1,  0,  0,  0,  0, -1,  0,  0,  0, -1},
                {-1,  0,  0,  0,  0,  0,  0,  0,  0, -1},
                {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
        };
        Node start = new Node(4, 2);
        map[start.x][start.y]=1;
        start.Father = null;
        Node end = new Node(4, 7);
        map[end.x][end.y]=2;
```
* 调用Solution函数，拿到最短路径
```java
Solution solution = new Solution();
        Node res_node = solution.astarSearch(start, end);//res—node最先接收的是该地图的终止
        while (res_node != null) {
            map[res_node.x][res_node.y] = res_node.G;
            res_node = res_node.Father;//迭代操作，从终止结点开始向后退，直到起点的父节点为null。循环终止
        }
```
* 输出路径，我在这里对输出地图进行了一定程度上的渲染，红色代表边界和障碍不可达点，绿色代表起点和终点，蓝色代表路径，数字代表所走的步数（我在这里进行了改进，使输出地图更加直观）。<br>
```java
//渲染迷宫
        for (int i = 0; i < 10; i++)
        {
            for (int j = 0; j < 10; j++)
            {
                Node nownode =new Node(i,j);
                if(map[i][j]==-1)
                    System.out.printf("\033[31m%3d\033[0m", map[i][j]);//red
                else if (equal_node(nownode,start) || equal_node(nownode,end))
                    System.out.printf("\033[32m%3d\033[0m", map[i][j]);//green
                else if(map[i][j]==0 && !equal_node(nownode,start))
                    System.out.printf("%3d", map[i][j]);//black
                else
                    System.out.printf("\033[34m%3d\033[0m", map[i][j]);//blue
            }
            System.out.println();
        }
```
### 3.4 输出结果
<br>
<div style="text-align: center;">
    <img src="https://i-blog.csdnimg.cn/direct/2946267bd1cc482fb318cd88c5a3abca.png" alt="Image" />
</div>

[源代码已保存到课题组github文件夹](https://github.com/Asurada2015/PingShanWu/tree/main/AStar)