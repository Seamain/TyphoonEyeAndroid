package seamain.org.typhoonEye.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface TyphoonDao {
    @Query("SELECT * FROM typhoons ORDER BY cachedAtEpochMs DESC")
    suspend fun getAll(): List<TyphoonEntity>

    @Query("SELECT * FROM typhoons WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TyphoonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<TyphoonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TyphoonEntity)

    @Query("DELETE FROM typhoons")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(entities: List<TyphoonEntity>) {
        clear()
        if (entities.isNotEmpty()) {
            upsertAll(entities)
        }
    }
}
