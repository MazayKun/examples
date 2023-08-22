package ru.otus;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.core.repository.DataTemplate;
import ru.otus.core.repository.executor.DbExecutorImpl;
import ru.otus.core.sessionmanager.TransactionRunnerJdbc;
import ru.otus.crm.datasource.DriverManagerDataSource;
import ru.otus.crm.model.Client;
import ru.otus.crm.model.Manager;
import ru.otus.crm.service.DbServiceClientImpl;
import ru.otus.crm.service.DbServiceManagerImpl;
import ru.otus.jdbc.mapper.*;
import ru.otus.statistic.TimeMeasurerFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class HomeWork {
    private static final String URL = "jdbc:postgresql://localhost:5430/demoDB";
    private static final String USER = "usr";
    private static final String PASSWORD = "pwd";

    private static final Logger log = LoggerFactory.getLogger(HomeWork.class);

    public static void main(String[] args) {
// Общая часть
        var dataSource = new DriverManagerDataSource(URL, USER, PASSWORD);
        flywayMigrations(dataSource);
        var transactionRunner = new TransactionRunnerJdbc(dataSource);
        var dbExecutor = new DbExecutorImpl();

// Работа с клиентом
        EntityClassMetaData<Client> entityClassMetaDataClient = new EntityClassMetaDataImpl<>(Client.class);
        EntitySQLMetaData entitySQLMetaDataClient = new EntitySQLMetaDataImpl(entityClassMetaDataClient);
        DataTemplate<Client> dataTemplateClient = new DataTemplateJdbc<>(dbExecutor, entitySQLMetaDataClient); //реализация DataTemplate, универсальная
        dataTemplateClient = TimeMeasurerFactory.getInstance(dataTemplateClient, DataTemplate.class);

// Код дальше должен остаться
        var dbServiceClient = new DbServiceClientImpl(transactionRunner, dataTemplateClient);
        dbServiceClient.saveClient(new Client("dbServiceFirst"));

        var clientSecond = dbServiceClient.saveClient(new Client("dbServiceSecond"));
        var clientSecondSelected = dbServiceClient.getClient(clientSecond.getId())
                .orElseThrow(() -> new RuntimeException("Client not found, id:" + clientSecond.getId()));
        //log.info("clientSecondSelected:{}", clientSecondSelected);
        dbServiceClient.findAll();
// Сделайте тоже самое с классом Manager (для него надо сделать свою таблицу)

        EntityClassMetaData<Manager> entityClassMetaDataManager = new EntityClassMetaDataImpl<>(Manager.class);
        EntitySQLMetaData entitySQLMetaDataManager = new EntitySQLMetaDataImpl(entityClassMetaDataManager);
        DataTemplate<Manager> dataTemplateManager = new DataTemplateJdbc<>(dbExecutor, entitySQLMetaDataManager);
        dataTemplateManager = TimeMeasurerFactory.getInstance(dataTemplateManager, DataTemplate.class);

        var dbServiceManager = new DbServiceManagerImpl(transactionRunner, dataTemplateManager);
        dbServiceManager.saveManager(new Manager("ManagerFirst"));

        var managerSecond = dbServiceManager.saveManager(new Manager("ManagerSecond"));
        var managerSecondSelected = dbServiceManager.getManager(managerSecond.getNo())
                .orElseThrow(() -> new RuntimeException("Manager not found, id:" + managerSecond.getNo()));
        //log.info("managerSecondSelected:{}", managerSecondSelected);
        dbServiceManager.findAll();

        Client currentClient;
        List<Long> ids = new ArrayList<>(5);
        for(int i = 0; i < 5; i++) {
            currentClient = dbServiceClient.saveClient(new Client("TEST"));
            ids.add(currentClient.getId());
        }
        long id = ids.get(0);
        int i = 0;
        for(; id <= ids.get(4); i++, id += (i % 2 == 0 ? 1 : 0)) {
            dbServiceClient.getClient(id);
        }
        for (Long currId : ids) {
            dbServiceClient.saveClient(new Client(currId, "NEW TEST"));
        }
        for(id = ids.get(0), i = 0; id <= ids.get(4); i++, id += (i % 2 == 0 ? 1 : 0)) {
            dbServiceClient.getClient(id);
        }
    }

    private static void flywayMigrations(DataSource dataSource) {
        //log.info("db migration started...");
        var flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:/db/migration")
                .load();
        flyway.migrate();
        //log.info("db migration finished.");
        //log.info("***");
    }
}
