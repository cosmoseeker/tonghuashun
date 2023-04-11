>>假设一个股票财务实体，financeEntity（code,date,income,profit,roe) 有100个字段。
估计有2w个实体。每个实体600期数据（比如一季报、半年报、三季报、年报就是4期）；

使用原生代码实现一个高性能（单节点QPS>15000，评估合适的硬件资源配置）的服务，实现2W个实体支持任意指标字段排序的单期排行榜。


例如 按ROE取2022年度排名前10的股票；

1. 考虑支持其他类似financeEntity的实体；
2. 需要考虑实时更新；