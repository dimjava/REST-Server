package com.reshigo.service;

import com.reshigo.ServiceTestContext;
import org.dbunit.DataSourceBasedDBTestCase;
import org.dbunit.dataset.IDataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;

@ContextConfiguration(classes = {ServiceTestContext.class})
public class DBUnitConfig extends DataSourceBasedDBTestCase {
    @Autowired
    private DataSource dataSource;

    protected IDataSet dataSet;

    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        return dataSet;
    }
}
