
package es.caib.enviafib.model.fields;
import org.fundaciobit.genapp.common.query.*;
public interface SerieDocumentalFields extends java.io.Serializable {

  public static final String _TABLE_SQL = "efi_seriedocu";


  public static final String _TABLE_MODEL = "serieDocumental";


  public static final String _TABLE_TRANSLATION = _TABLE_MODEL + "." + _TABLE_MODEL;


	 public static final LongField SERIEDOCUID = new LongField(_TABLE_MODEL, "seriedocuid", "seriedocuid");  // PK
	 public static final StringField NOM = new StringField(_TABLE_MODEL, "nom", "nom");
	 public static final StringField TIPUSDOCU = new StringField(_TABLE_MODEL, "tipusdocu", "tipusdocu");


  public static final Field<?>[] ALL_SERIEDOCUMENTAL_FIELDS = {
    SERIEDOCUID,
    NOM,
    TIPUSDOCU
  };


  public static final Field<?>[] PRIMARYKEY_FIELDS = {
SERIEDOCUID
  };
}
