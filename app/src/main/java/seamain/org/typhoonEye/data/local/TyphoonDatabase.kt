package seamain.org.typhoonEye.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TyphoonEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TyphoonDatabase : RoomDatabase() {
    abstract fun typhoonDao(): TyphoonDao

    companion object {
        private const val DB_NAME = "typhoon_eye.db"

        fun create(context: Context): TyphoonDatabase =
            Room.databaseBuilder(context.applicationContext, TyphoonDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }
}
