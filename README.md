# tddl-sequence

参考[alibaba/tb_tddl](https://github.com/alibaba/tb_tddl)

实际从项目[cpsing/tddl](https://github.com/cpsing/tddl)
中提取序列部分[参考代码](https://github.com/cpsing/tddl/tree/master/tddl-sequence)

## 说明

- 不保证序列连续
- 支持集群部署
- db脚本适配为MySql
- 程序核心为内存缓存，重启后直接从数据库获取下一段序列
- 缓存有上限，根据物理机决定，自行测试
- 当序列过多时建议根据序列名在网关进行路由，一个服务/集群只负责一部分的序列生成