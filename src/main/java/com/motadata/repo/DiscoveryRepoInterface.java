package com.motadata.repo;


import java.sql.SQLException;

public interface DiscoveryRepoInterface<T, ID> {
    void Create(T entry) throws SQLException;
    void Delete(ID id);
    void update(ID id, T entry);
    T read(ID id);

    //JsonArray readALl();
}
