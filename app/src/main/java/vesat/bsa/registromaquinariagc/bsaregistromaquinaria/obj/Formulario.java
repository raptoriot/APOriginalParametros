package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj;

import org.json.JSONObject;

import java.io.Serializable;

import vesat.bsa.registromaquinariagc.bsaregistromaquinaria.lib.Util;

public class Formulario implements Serializable {

    public String id;
    public String nombre;
    public String definicion;
    public String primary_fields;

    public void loadFromJSON(JSONObject jsondata) {
        id = Util.getJSONStringOrNull(jsondata,"id");
        nombre = Util.getJSONStringOrNull(jsondata,"nombre");
        definicion = Util.getJSONStringOrNull(jsondata,"definicion");
        primary_fields = Util.getJSONStringOrNull(jsondata,"primary_fields");
    }
}
