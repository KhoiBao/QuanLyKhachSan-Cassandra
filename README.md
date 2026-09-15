# QuanLyKhachSan-Cassandra

Quan ly khach san su dung Spring Boot + Cassandra.

## Cong nghe
- Java 21
- Spring Boot 4.1.1
- Spring Data Cassandra
- Spring Web MVC
- Maven Wrapper

## Cau truc so do cay du an

```text
QuanLyKhachSan-Cassandra/
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── qlkhachsan/
│   │   │           └── QlkhachsanApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/
│               └── qlkhachsan/
│                   └── QlkhachsanApplicationTests.java
├── .gitattributes
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

### Giai thich
- `.mvn/` - Cau hinh Maven Wrapper.
- `src/main/java/com/qlkhachsan/` - Ma nguon chinh, chua class `QlkhachsanApplication`.
- `src/main/resources/application.properties` - Cau hinh app (Cassandra, port...).
- `src/test/` - Test khoi dong context Spring Boot.
- `pom.xml` - Khai bao dependency Spring Boot, Cassandra, WebMVC.
- `mvnw`, `mvnw.cmd` - Chay Maven khong can cai san.
