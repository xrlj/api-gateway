# xrlj-gateway

## docker启动

1.开发环境

    docker run --name api-gateway -e "SPRING_PROFILES_ACTIVE=dev" -e "EUREKA_INSTANCE_IP-ADDRESS=172.31.31.31" -p 5555:5555 -v /apps/api-gateway/tmp:/tmp -v /apps/api-gateway/logs:/logs -d 192.168.1.110:8082/xrlj/api-gateway:0.0.1