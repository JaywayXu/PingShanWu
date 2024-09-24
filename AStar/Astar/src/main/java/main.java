import java.util.ArrayList;
import java.util.PriorityQueue;

public class main {

    public static  boolean equal_node(Node node1 , Node node2){
        if(node1.x== node2.x && node1.y == node2.y)
            return true;
        return false;
    }

    public static void main(String[] args) {
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
        Solution solution = new Solution();
        Node res_node = solution.astarSearch(start, end);//res—node最先接收的是该地图的终止
        while (res_node != null) {
            map[res_node.x][res_node.y] = res_node.G;
            res_node = res_node.Father;//迭代操作，从终止结点开始向后退，直到起点的父节点为null。循环终止
        }
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
    }
}
//结点的属性
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


//Astar具体实现方法
class Solution {
    //地图 -1 代表墙壁， 1代表起点，2代表终点
    public int[][] map = {
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
    // Open表用优先队列
    public PriorityQueue<Node> Open = new PriorityQueue<Node>();
    //Close表用普通的数组
    public ArrayList<Node> Close = new ArrayList<Node>();
    //Exist表用来存放已经出现过的结点。
    public ArrayList<Node> Exist = new ArrayList<Node>();

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

    public boolean is_exist(Node node)
    {
        for (Node exist_node : Exist) {
            if (node.x == exist_node.x && node.y == exist_node.y) {
                return true;
            }
        }
        return false;
    }
}


