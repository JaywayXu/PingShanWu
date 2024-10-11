# 基于MOA*的多目标路径规划问题之我见
* 最近在对基础单目标Astar算法有所了解后，笔者进一步探索和学习了多目标Astar算法，即MOAstar。在由单目标变成多目标的研究过程中，笔者经历了一些疑问和思考，也收获了很多体会和感悟，于是在这里记录一下笔者的学习过程。
* [基础单目标A*算法学习链接](https://blog.csdn.net/u013555719/article/details/142487884)
* 此为课题组2024级研究生吴屏珊同学近期学习内容汇报
- 更多内容请关注课题组官方中文主页：https://JaywayXu.github.io/zh-cn/

## 目录
[TOC]
<br>
## 1. 单目标A\*与多目标A\*的区别
### 1.1 目标上的区别
1、单目标A\*的目标：从起点走到终点，其中要求所走距离最短并且要绕开所有不可达点。
2、多目标A\*的目标：（1）顾名思义，多目标就是在单目标的基础上 **增加目标约束** ,要求在多个目标上同时进行优化，考虑多个目标的最小化，包括但不限于，拥堵点数量最少，所经过交叉点数量最少，路径目标值最小，路径必经点最多。（2）在单目标的基础上 **找到Pareto解集** ，在学习基础单目标A\*算法中，我们最终得到的目标值最小的路径只有一条，即使可能存在多条目标值相同的路径，但算法一旦找到终点将不再进行下去，直接输出结果。而在MOA\*算法中，我们会在找到所有非支配路径（Pareto解集），即目标值等同的最优路径后进行输出。
### 1.2 单目标算法的核心F=G+H在多目标中的体现
1、首先，在多目标算法中F，G，H，各值的意义没有改变。F仍为总代价，G仍为从起点到该点已走过路径的代价，H仍为从该点到终点将付出的代价。只是目标的表现形式不再是单一的距离目标，而是需要我们 **根据不同的目标建立不同的目标向量矩阵** ，考虑多个维度的目标值。
2、G值在多目标A\*算法中意义的升级：G值为已走路径的目标值。
3、H值在多目标A\*算法中意义的升级：H值由启发式函数确定。
## 2. 多目标A\*算法中G值和H值的实现
### 2.1 G值的计算
1、首先，设置一个特殊点的概念，特殊点包含了起点，终点和路径交叉点。
2、遍历所有特殊点，并为每一个特殊点设置两个邻接表，分别存储该特殊点的出边与入边。
3、在出边邻接表和入边邻接表中都存储着两个相邻特殊点之间的 Edge ，而在 Edge 中的 edge_cost 数组存储着从特殊点 A 到特殊点 B 这条路径的各目标分量。edge_pass 则是存储着从特殊点 A 到特殊点 B 具体经过的非特殊点集合。
4、因此，已走路径的各目标分量，即G值，可以通过 **累加路径所经过的特殊点的邻接表中对应的edge_cost数组的值** 得来。
```C++
for (int prev_node = 0; prev_node < dim_n; prev_node++) {//遍历每个特殊节点
      auto [x, y] = coords[prev_node];//获取当前节点的坐标
      for (const auto &[step_x, step_y] : steps) {//遍历四个方向走
        if (within(x + step_x, y + step_y, dim_x, dim_y) &&
            retained[x + step_x][y + step_y]) {//检查移动后的位置是否在范围内并且是可通行的
          Cost edge_cost(dim_c, 0);//定义边的成本
          Path edge_pass;//经过的路径
          int prev_x = x;//当前坐标也是将要成为的前置坐标
          int prev_y = y;
          int curr_x = x + step_x;//下一个坐标也是将要成为的当前坐标
          int curr_y = y + step_y;
          while (node_matrix[curr_x][curr_y] == -1) {//是除了起终管岔路以外的所有点
            for (int c = 0; c < dim_c; c++) {//dim_c拥塞维度
              edge_cost[c] += cost_matrix[curr_x][curr_y][c];//累加当前位置的成本到边的成本中
            }
            edge_pass.emplace_back(curr_x, curr_y);//将当前位置添加到边的路径中，非特殊点

            for (const auto &[next_step_x, next_step_y] : steps) {//尝试在当前位置的周围找到下一个可通行的位置继续前进
              if (within(curr_x + next_step_x, curr_y + next_step_y, dim_x,
                         dim_y) &&
                  retained[curr_x + next_step_x][curr_y + next_step_y] &&
                  !(curr_x + next_step_x == prev_x &&
                    curr_y + next_step_y == prev_y)) {
                prev_x = curr_x;
                prev_y = curr_y;
                curr_x += next_step_x;//更新current点进入循环
                curr_y += next_step_y;
                break;
              }
            }
          }//while的终止条件是找到下一个特殊点，所以while是用来找到从当前特殊点到下一个特殊点，所经过的路径path和一路上每个维度所需代价

          int next_node = node_matrix[curr_x][curr_y];//现在的current是之前特殊节点可到达的下一个特殊节点
          int edge_id = edges.size();
          out_edges[prev_node].push_back(edge_id);//原来特殊节点的出边
          in_edges[next_node].push_back(edge_id);//现在特殊节点的入边
          edges.push_back(
              {prev_node, next_node, move(edge_cost), move(edge_pass)});
        }//记录这一路上的代价和所走路径
      }//当前特殊节点四个方向全部遍历完
    }//end for 所有特殊节点遍历完，构成一个包含特殊节点及其之间连接关系的图，最终的图结构通过邻接表的方式表示，分别记录每个节点的出边和入边，不仅包括这些节点，还反映了节点之间的路径成本。
```
### 2.2 启发式函数H值的计算
1、首先，设置一个 ideal_to_goal 函数，返回的是每一个特殊点到终点的最短路径目标向量，即目标下界，其中最短路径成本是多维度的。
2、运用 Dijkstra 算法从终点开始，遍历入边邻接表找到该特殊节点的 “Father” 并累加路径的 edge_cost ，最后构建 ideal_to_goal 矩阵存储每个特殊节点的返回结果。
3、在不考虑必经点的情况下，该点到终点将付出的各目标分量，即H值，可以通过调用该点的 ideal_to_goal 函数得知。
```C++
vector<Cost> ideal_to_goal(dim_n, Cost(dim_c, 1e9));
    for (int c = 0; c < dim_c; c++) {
      ideal_to_goal[goal_node][c] = 0;//设置 goal_node 到自身的成本为 0，因为终点到终点的路径代价为 0。
      set<pair<double, int>> pq;
      pq.emplace(0, goal_node);
      while (!pq.empty()) {
        auto [dist, node] = *pq.begin();
        pq.erase(pq.begin());
        for (int edge_id : in_edges[node]) {
          int prev_node = edges[edge_id].prev_node;
          double new_prev_dist = edges[edge_id].edge_cost[c] + dist;
          if (node != goal_node) {
            new_prev_dist += costs[node][c];
          }
          if (ideal_to_goal[prev_node][c] == 1e9) {
            ideal_to_goal[prev_node][c] = new_prev_dist;
            pq.emplace(new_prev_dist, prev_node);
          } else if (new_prev_dist < ideal_to_goal[prev_node][c]) {
            pq.erase({ideal_to_goal[prev_node][c], prev_node});
            ideal_to_goal[prev_node][c] = new_prev_dist;
            pq.emplace(new_prev_dist, prev_node);
          }
        }
      }
    }
```
## 3. 多目标A*算法的主要实现
### 3.1 支配关系的定义
1、一般来说，MOP可以表示为：${Minimize  F(x) =\lbrace{f_1(x),f_2(x),···,f_m(x)\rbrace}}$ ， ${x=(x_1,x_2,...,x_n)\in\Omega}$，其中 ${\Omega}$ 表示搜索空间，m 是目标的数量，x 是由 n 个决策变量 ${x_i}$ 组成的决策向量。一个解 ${x_a}$ 被认为 Pareto 支配另一个解 ${x_b}$ 当且仅当 ${\forall i =1,2,...,m,f_i(x_a) \leq f_i(x_b)}$ 并且 ${\exists j=1,2,...m,f_j(x_a) \lt f_j(x_b)}$。
2、 在多目标A\*算法中，支配关系的判断是一条路径 A 的各目标分量要不大于另一条路径 B 的各目标分量，并且 A 至少要有一个目标分量严格小于 B 对应的目标分量。
### 3.2 多目标A*算法中个体的更迭
1、 首先，设置一个节点-状态对 ${(n,s)}$ ，一个节点-状态-代价三元组 ${(n,s,c)}$ ，定义 ${T_{n,s}}$ 存储在搜索期间的任何时刻从起始节点-状态对 ${(n_{start},s_{start})}$ 到节点-状态对 ${(n,s)}$ 的所有已识别路径的非支配目标向量。
2、遍历节点 n 的所有可到达的相邻子节点 ${n'}$ ，并得到子节点的节点-状态-代价三元组 ${(n',s',c')}$ 。
3、${c'}$ 被 ${T_{n',s'}}$ 支配，则证明当前路径不如已有路径，跳过。
4、${c'}$ 支配了 ${T_{n',s'}}$ 中的某些 ${\bar c}$ ，则证明有比 ${\bar c}$ 更好的解，并且这些被支配的 ${\bar c}$ 将在 ${T_{n',s'}}$ 中被删除。
5、如果 ${c'}$ 既不被 ${T_{n',s'}}$ 支配 ，也不支配 ${T_{n',s'}}$ 中的任何解，即以上两种情况都不符合， ${c'}$ 与 ${T_{n',s'}}$ 中的所有解是非支配关系，则计算 ${(n',s')}$ 的 F 值，并加入到 ${T_{n',s'}}$ 。
### 3.3 多目标A*算法的主体流程
1、设置一个优先队列 Open 表，每次将F值最小的点放在队首。（与单目标A\*算法相同）。
2、将起始节点放入到 Open 表中，开始遍历 Open 表，直到 Open 表中所有元素遍历完（Open 表为空）停止。这样可以保证算法能够找到所有非支配路径。（单目标A\*算法中遍历到终点就停止）
3、判断遍历到的节点是否为终点，是则存储并跳过进行下一轮循环，否则继续。
4、当前点的 F 值是否被被Pareto前沿支配，是则不考虑这个路径并跳过，否则继续。
5、当前点既不为重点，又不被Pareto前沿支配，则遍历当前节点的所有出边，判断父节点与子节点的支配关系（详见3.2），若两者非支配，则计算子节点的 F 值，并加入到Open中。
```C++
const Cost &start_cost = costs[start_node];//获取起始节点的成本

    Cost start_est = start_cost + heuristic(start_node, 0);//计算起始节点的估计总成本

    vector<unordered_map<int, Front>> tentative(dim_n);
    tentative[start_node][0][start_cost] = {start_est, {}};//初始化起始节点的临时前沿

    const Front &pareto_front = tentative[goal_node][(1 << dim_k) - 1];// 获取目标节点的Pareto前沿

    map<Cost, set<Triplet, TripletComparator>, CostComparator> open;//创建一个开放列表，用于存储待探索的节点。按照估计总成本进行排序
    open[start_est].insert({start_node, 0, costs[start_node]});//将起始节点添加到开放列表中。

    int iteration = 0;
    while (!open.empty()) {
      iteration++;
      Cost est = open.begin()->first;//从起点到当前节点的实际代价加上从当前节点到目标节点的估计代价之和
      auto [node, status, cost] = *open.begin()->second.begin();//获取开放列表中估计总成本最小的节点信息

      if (auto it = open.begin(); it->second.size() == 1) {
        open.erase(open.begin());//如果集合中只有一个节点，open 列表中这个键值对（节点和它的代价）会被删除
      } else {//如果有多个节点具有相同的估计成本，则只删除当前处理的节点，保留剩下的节点。
        it->second.erase(it->second.begin());
      }

      if (node == goal_node && status == (1 << dim_k) - 1) {//如果当前节点是目标节点，并且已经访问了所有关键点（状态为全1），则继续下一轮循环，意味着找到了有效路径。
        continue;
      }

      if (pareto_front.dominates(est)) {//如果当前节点的估计成本被Pareto前沿支配，则不考虑这个路径，直接跳过。
        tentative[node][status].erase(cost);
        continue;
      }

      //通过扩展节点、更新状态、计算成本和启发式估计，动态寻找多目标条件下的最优路径。同时，支配（dominates）的概念帮助过滤次优路径，从而提高效率。
      for (int edge_id : out_edges[node]) {//遍历当前节点的所有出边
        int next_node = edges[edge_id].next_node;

        int next_status = status;
        if (keys[next_node] != -1) {//对于每条边，获取下一个节点和更新状态（如果该节点是关键点，则更新状态）。
          next_status |= (1 << keys[next_node]);
        }

        Cost next_cost = cost;
        next_cost += edges[edge_id].edge_cost;
        next_cost += costs[next_node];//计算到达下一个节点的总成本。

        Front &next_tent = tentative[next_node][next_status];

        if (next_tent.dominates(next_cost)) {//检查当前路径是否已经支配了下一个节点的路径，如果是，则跳过
          continue;
        }

        if (auto it = next_tent.find(next_cost); it != next_tent.end()) {
          it->second.previous.push_back({node, status, cost, edge_id});
          continue;//将当前路径信息加入到previous中以备后续路径回溯使用
        }

        if (auto it_dom = next_tent.find_dominated(next_cost);//对于每个被支配的路径，从临时前沿中删除，确保只保留最优路径
            it_dom != next_tent.end()) {
          vector<Cost> dominated;
          while (it_dom != next_tent.end()) {//找到了被支配的路径
            const Cost &dom_cost = it_dom->first;//被支配路径的 Cost 值
            const Cost &dom_est = it_dom->second.est;//估计总成本
            if (auto it_open = open.find(dom_est); it_open != open.end()) {//删除劣于当前路径的其他路径
              if (auto it_set =
                      it_open->second.find({next_node, next_status, dom_cost});
                  it_set != it_open->second.end()) {//从 open 列表中删除被支配路径
                if (it_open->second.size() == 1) {
                  open.erase(it_open);//当前估计代价 dom_est 下只有一个节点组合，则删除整个 open 项
                } else {//只有同node，同status下的同dom_cost才会被删除，可能包含有相同dom_cost，但是是不同node，status的，因为set集合是以dom_cost作为查找条件的
                  it_open->second.erase(it_set);// 仅删除当前被支配的路径组合,而不会影响其他具有相同估计代价的节点组合
                }
              }
            }
            dominated.push_back(dom_cost);
            it_dom++;
          }
          for (const Cost &dom_cost : dominated) {//从临时前沿中删除被支配路径
            next_tent.erase(dom_cost);
          }
        }

        Cost next_est = next_cost + heuristic(next_node, next_status);//计算估计的成本
        if (pareto_front.dominates(next_est)) {//是否已经被帕累托前沿支配
          continue;
        }

        open[next_est].insert({next_node, next_status, next_cost});//添加到开放列表中

        next_tent[next_cost] = {next_est, {{node, status, cost, edge_id}}};
      }//end for 遍历完当前节点下一步可达的所有点
    }//end while 所有open遍历完
```

## 📎 Homepages

- Personal Pages: https://JaywayXu.github.io (updated recently🔥)
- 中文站点：https://JaywayXu.github.io/zh-cn/ （`如果你是国内的学者，想要更多了解我们课题组的动态`）
- Google Scholar: https://scholar.google.com/citations?user=_Lkioz8AAAAJ&hl
- Researchgate: https://www.researchgate.net/profile/Zhiwei-Xu-16
- 微信公众号： 演化计算与人工智能
- CSDN: 武科大许志伟 : https://xuzhiwei.blog.csdn.net/
- Email: xuzhiwei@wust.edu.cn

My full paper list is shown at [my personal homepage](https://JaywayXu.github.io/) or [中文主站](https://JaywayXu.github.io/zh-cn/).