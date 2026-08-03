package seamain.org.typhoonEye.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "typhoons")
data class TyphoonEntity(
    @PrimaryKey val id: String,
    val name: String,
    val englishName: String,
    val status: String,
    val strong: String,
    val positionDesc: String,
    val forecastText: String,
    val startTime: String,
    val endTime: String,
    /** JSON array of TyphoonPointDto */
    val pointsJson: String,
    /** JSON array of TyphoonPointDto */
    val forecastPointsJson: String,
    val cachedAtEpochMs: Long
)
