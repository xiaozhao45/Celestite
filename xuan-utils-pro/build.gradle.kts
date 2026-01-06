plugins {
    id("java-library")
}

group = "xuan"
version = "2.0-Modify"

java {
    // 设置为 Java 11，这是目前 Android Studio 和 Compose Multiplatform 兼容性最好的版本
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

// ⚠️ 关键设置：强制使用 UTF-8 编码，防止玄学术语（中文）乱码
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
}

dependencies {
    // ==========================================
    // 核心算法依赖 (必须保留)
    // ==========================================
    
    // 农历/阴阳历核心库
    implementation("cn.6tail:lunar:1.7.4")
    
    // IP2Region 离线定位库
    implementation("org.lionsoul:ip2region:1.7.2")

    // ==========================================
    // 工具类库 (保留，用于数据处理和通用逻辑)
    // ==========================================

    // 阿里巴巴 FastJSON
    implementation("com.alibaba:fastjson:1.2.58")

    // Apache Commons 系列 (很多 Util 类通常依赖这些)
    implementation("commons-io:commons-io:2.6")
    implementation("commons-lang:commons-lang:2.6")
    implementation("commons-validator:commons-validator:1.6")
    implementation("org.apache.commons:commons-lang3:3.8.1")

    // Jackson (POM中原有，先保留以防报错。如果编译成功，建议后续尝试移除以减小体积)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")

    // ==========================================
    // Lombok 配置 (编译时生成代码，不打包进APK)
    // ==========================================
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    
    // 测试环境也需要 Lombok
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    // ==========================================
    // 测试依赖
    // ==========================================
    testImplementation("junit:junit:4.13.2")
}

// ==========================================
// 说明：已剔除的臃肿依赖
// ==========================================
// 1. spring-core: 体积过大且非必要。
//    如果编译报错提示找不到 `org.springframework.util.StringUtils`，
//    请手动修改 Java 代码，将其替换为 `org.apache.commons.lang3.StringUtils`。
// 2. tomcat-embed-core: Web服务器核心，Android 无法运行且不需要。