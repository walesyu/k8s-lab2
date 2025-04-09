# Lab 2: 使用 Azure Cache for Redis 和 ZooKeeper 提升應用程式效能和可靠性

## 目標
讓學生了解如何使用 Azure Cache for Redis 提升應用程式效能，並使用 ZooKeeper 實現分散式協調。

## 步驟

1.  **建立 Azure Cache for Redis 執行個體：**
    * 在 Azure 入口網站中建立一個 Azure Cache for Redis 執行個體。
    * 記下主機名稱、連接埠和存取金鑰。

2.  **建立 ZooKeeper 叢集：**
    * 可以使用 Azure HDInsight 建立一個 ZooKeeper 叢集。
    * 記下 ZooKeeper 仲裁的連接字串（例如：`zk0-xxx.azurehdinsight.net:2181,zk1-xxx.azurehdinsight.net:2181`）。

3.  **修改 Java 應用程式：**
    * **Redis 快取：** `CacheService.java` 封裝了與 Redis 的互動，可以進行資料的讀取和寫入。
    * **配置管理 (ZooKeeper)：** `ConfigurationService.java` 使用 ZooKeeper 儲存和檢索應用程式的配置資訊。
    * **服務發現 (ZooKeeper)：** `ServiceDiscoveryService.java` 演示了如何使用 ZooKeeper 註冊和發現服務實例。
    * `App.java` 是應用程式的主程式，演示了如何使用 `CacheService`、`ConfigurationService` 和 `ServiceDiscoveryService`。

4.  **測試應用程式的效能和可靠性：**
    * 運行 `App.java`。
    * 觀察日誌輸出，了解資料如何從快取中獲取或被快取。
    * 嘗試修改 ZooKeeper 中的配置，觀察應用程式是否能讀取到新的配置。
    * （進階）可以模擬服務實例的啟動和停止，觀察服務發現機制。

## 學習重點

* **Azure Cache for Redis 的基本概念：** 記憶體資料儲存、鍵值對。
* **快取策略：** 讀取快取、寫入快取、失效策略（範例中使用過期時間）。
* **ZooKeeper 的基本概念：** 分散式協調服務、節點（ZNode）、監聽器（Watcher）。
* **分散式協調：**
    * **配置管理：** 將配置資訊集中管理在 ZooKeeper 上，方便動態更新和