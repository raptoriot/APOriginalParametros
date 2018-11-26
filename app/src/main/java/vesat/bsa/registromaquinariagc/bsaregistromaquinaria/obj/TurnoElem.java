package vesat.bsa.registromaquinariagc.bsaregistromaquinaria.obj;

public class TurnoElem {

    public String date_init;
    public String date_end;
    public int index;
    public long millis_init;
    public long millis_end;
    public int counter = 1;

    public TurnoElem(String date_init,String date_end,int index,long millis_init,long millis_end)
    {
        this.date_init = date_init;
        this.date_end = date_end;
        this.index = index;
        this.millis_init = millis_init;
        this.millis_end = millis_end;
    }
}
