package es.caib.enviafib.logic;

import java.io.IOException;
import java.util.List;

import javax.ejb.Local;

import org.fundaciobit.apisib.apifirmasimple.v1.beans.FirmaSimpleSignatureResult;
import org.fundaciobit.apisib.core.exceptions.AbstractApisIBException;
import org.fundaciobit.genapp.common.StringKeyValue;
import org.fundaciobit.genapp.common.i18n.I18NException;

import es.caib.enviafib.ejb.PeticioService;
import es.caib.enviafib.model.entity.Peticio;
import es.caib.enviafib.persistence.PeticioJPA;

/**
 * 
 * @author fbosch
 * @author anadal
 *
 */
@Local
public interface PeticioLogicaService extends PeticioService {

    public static final String JNDI_NAME = "java:app/enviafib-ejb/PeticioLogicaEJB!es.caib.enviafib.logic.PeticioLogicaService";

    public void arrancarPeticio(long peticioID, String languageUI) throws I18NException;

    public List<StringKeyValue> getAvailableTipusDocumental(String lang) throws I18NException;

    public void esborrarPeticioPortafib(long peticioPortafibId, String languageUI) throws Exception;

    public void updatePublic(Peticio peticio) throws I18NException;

    public PeticioJPA findByPrimaryKeyPublic(Long _ID_);

    public void deleteFull(Peticio instance) throws I18NException;

    public long guardaInformacioSignatura(long peticioID, String languageUI) throws I18NException, AbstractApisIBException;
    
    public long guardarFitxerSignat(long peticioID, String languageUI) throws I18NException, AbstractApisIBException, IOException;
    
    public void guardarResultatAutofirma(long peticioID, FirmaSimpleSignatureResult fssr) throws I18NException;
}
