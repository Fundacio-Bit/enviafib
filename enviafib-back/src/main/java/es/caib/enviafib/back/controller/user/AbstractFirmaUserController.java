package es.caib.enviafib.back.controller.user;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.fundaciobit.genapp.common.i18n.I18NException;
import org.fundaciobit.genapp.common.query.Field;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.ModelAndView;

import es.caib.enviafib.back.form.webdb.PeticioForm;
import es.caib.enviafib.commons.utils.Constants;
import es.caib.enviafib.model.entity.Peticio;
import es.caib.enviafib.model.fields.PeticioFields;
import es.caib.enviafib.model.fields.UsuariFields;
import es.caib.enviafib.persistence.PeticioJPA;

/**
 *
 * @author anadal
 *
 */
public abstract class AbstractFirmaUserController extends AbstractPeticioUserController {

    @Override
    public boolean isActiveList() {
        return false;
    }

    @Override
    public boolean isActiveDelete() {
        return false;
    }

    @Override
    public boolean isActiveFormNew() {
        return true;
    }

    @Override
    public boolean isActiveFormEdit() {
        return true;
    }

    @Override
    public boolean isActiveFormView() {
        return true;
    }

    @Override
    public String getTileForm() {
        return "peticioFormUser";
    }

    public abstract int getTipusPeticio();

    @Override
    public PeticioForm getPeticioForm(PeticioJPA _jpa, boolean __isView, HttpServletRequest request,
            ModelAndView mav) throws I18NException {
        PeticioForm peticioForm = super.getPeticioForm(_jpa, __isView, request, mav);

        boolean init = true;
        if (__isView) {

            if (peticioForm.getPeticio().getEstat() == ESTAT_PETICIO_FIRMADA) {
                peticioForm.addHiddenField(ERROREXCEPTION);
                peticioForm.addHiddenField(ERRORMSG);
                peticioForm.addHiddenField(PETICIOPORTAFIRMES);
                peticioForm.addHiddenField(INFOSIGNATURAID);
                init = false;
            }
        }

        if (init) {
            Set<Field<?>> hiddens = new HashSet<Field<?>>(
                    Arrays.asList(PeticioFields.ALL_PETICIO_FIELDS));

            hiddens.remove(NOM);
            hiddens.remove(FITXERID);
            hiddens.remove(TIPUSDOCUMENTAL);
            hiddens.remove(IDIOMADOC);

            if (peticioForm.getPeticio().getEstat() == ESTAT_PETICIO_REBUTJADA) {
                hiddens.remove(ESTAT);
                hiddens.remove(ERRORMSG);
                hiddens.remove(ERROREXCEPTION);
            }

            peticioForm.setHiddenFields(hiddens);
        }

        if (peticioForm.isNou()) {

            Peticio peticio = peticioForm.getPeticio();

            peticio.setDatacreacio(new Timestamp(System.currentTimeMillis()));
            peticio.setEstat(Constants.ESTAT_PETICIO_CREADA);

            String userName = request.getRemoteUser();
            Long userId = usuariEjb.executeQueryOne(UsuariFields.USUARIID,
                    UsuariFields.USERNAME.equal(userName));
            peticio.setSolicitantID(userId);

            peticio.setTipus(getTipusPeticio());

            // Amagam el selector d'idioma a la creacio de peticio. S'agafa el del context
            // autmaticament.
            peticio.setIdiomaID(LocaleContextHolder.getLocale().getLanguage());

            // Idioma per defecte per els documents, catala.
            peticio.setIdiomadoc("ca");
        }

        return peticioForm;
    }

    @Override
    public String getRedirectWhenCreated(HttpServletRequest request, PeticioForm peticioForm) {
        return "redirect:" + LlistatPeticionsUserController.CONTEXT_WEB + "/list";
    }

    @Override
    public String getRedirectWhenModified(HttpServletRequest request, PeticioForm peticioForm,
            Throwable __e) {

        if (__e == null) {
            return "redirect:" + LlistatPeticionsUserController.CONTEXT_WEB + "/list";
        } else {
            return getTileForm();
        }
    }

    @Override
    public String getRedirectWhenCancel(HttpServletRequest request, java.lang.Long peticioID) {
        return "redirect:" + LlistatPeticionsUserController.CONTEXT_WEB + "/list";
    }

}
