#!/bin/bash
#构建docker 镜像并上传到私服
# mvn加上参数-U，强制更新，以防无法及时更新依赖包
mvn clean install -U dockerfile:build && mvn clean install -U dockerfile:push