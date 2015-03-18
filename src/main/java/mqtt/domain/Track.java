package mqtt.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.support.index.IndexType;

/**
 * Created by sfrensley on 3/12/15.
 * @Track is the domain model used to represent the MQTT message
 * from OwnTracks
 */
@NodeEntity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Track extends BaseEntity {

    //Name of "layer" index for neo4j spatial indexes
    public static final String wktIndexName = "MessageLocation";

    Number cog;
    Number lon;
    Number acc;
    Number vel;
    String _type;
    Number batt;
    Number vac;
    Number lat;
    String t;
    Number tst;
    Number alt;
    String tid;
    Session session;

    //WKT format - Well Known Text
    @Indexed(indexType = IndexType.POINT, indexName=wktIndexName)
    String wkt;

    public void setLon(Number lon) {
        this.lon = lon;
        setLocation(getLon(),getLat());
    }

    public void setLat(Number lat) {
        this.lat = lat;
        setLocation(getLon(),getLat());
    }

    public void setLocation(Number lon, Number lat) {
        this.lon = lon;
        this.lat = lat;
        if (lon != null && lat !=  null) {
            this.wkt = String.format("POINT( %.4f %.4f )", lon.doubleValue(), lat.doubleValue());
        }
    }

}
