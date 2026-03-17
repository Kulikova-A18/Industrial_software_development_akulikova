package com.tigerbank;

import com.tigerbank.di.DIContainer;
import com.tigerbank.domain.factory.DomainFactory;
import com.tigerbank.domain.factory.DomainFactoryImpl;
import com.tigerbank.exporter.CsvExporter;
import com.tigerbank.exporter.JsonExporter;
import com.tigerbank.facade.AnalyticsFacade;
import com.tigerbank.gui.MainFrame;
import com.tigerbank.repository.*;
import com.tigerbank.service.*;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            DIContainer container = createContainer();

            MainFrame app = new MainFrame(container);
            app.setVisible(true);
        });
    }

    private static DIContainer createContainer() {
        DIContainer container = new DIContainer();

        container.registerSingleton(DomainFactory.class, DomainFactoryImpl::new);

        OperationRepository operationRepo = new OperationRepository(null, null);
        BankAccountRepository accountRepo = new BankAccountRepository(operationRepo);
        CategoryRepository categoryRepo = new CategoryRepository(operationRepo);

        operationRepo = new OperationRepository(accountRepo, categoryRepo);

        container.registerInstance(BankAccountRepository.class, accountRepo);
        container.registerInstance(CategoryRepository.class, categoryRepo);
        container.registerInstance(OperationRepository.class, operationRepo);

        container.register(AccountService.class, () -> new AccountService(
                container.resolve(BankAccountRepository.class),
                container.resolve(OperationRepository.class)

        ));

        container.register(CategoryService.class, () -> new CategoryService(
                container.resolve(CategoryRepository.class)

        ));

        container.register(OperationService.class, () -> new OperationService(
                container.resolve(OperationRepository.class),
                container.resolve(AccountService.class),
                container.resolve(CategoryService.class)

        ));

        container.register(AnalyticsService.class, () -> new AnalyticsService(
                container.resolve(OperationService.class),
                container.resolve(CategoryService.class),
                container.resolve(AccountService.class)));

        container.register(AnalyticsFacade.class, () -> new AnalyticsFacade(
                container.resolve(AnalyticsService.class),
                container.resolve(OperationService.class),
                container.resolve(CategoryService.class),
                container.resolve(AccountService.class)));

        container.registerSingleton(CsvExporter.class, CsvExporter::new);
        container.registerSingleton(JsonExporter.class, JsonExporter::new);

        return container;
    }
}