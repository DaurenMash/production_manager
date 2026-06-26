prodman-desktop/
├── pom.xml
├── src/main/java/com/prodman/desktop/
│   ├── ProdManApplication.java
│   ├── config/
│   │   └── ApiConfig.java
│   ├── controller/
│   │   ├── LoginController.java
│   │   ├── MainController.java
│   │   ├── DashboardController.java
│   │   └── EquipmentController.java
│   ├── model/
│   │   ├── User.java
│   │   ├── AuthResponse.java
│   │   └── Equipment.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── EquipmentService.java
│   │   └── HttpClientService.java
│   ├── utils/
│   │   ├── TokenManager.java
│   │   └── AlertUtils.java
│   └── view/
│       ├── login.fxml
│       ├── main.fxml
│       ├── dashboard.fxml
│       └── equipment.fxml
└── src/main/resources/
└── application.properties