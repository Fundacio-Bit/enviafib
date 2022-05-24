package es.caib.enviafib.back.controller.webdb;

import org.fundaciobit.genapp.common.web.i18n.I18NUtils;
import org.fundaciobit.genapp.common.i18n.I18NException;
import org.fundaciobit.genapp.common.query.GroupByItem;
import org.fundaciobit.genapp.common.query.Field;
import org.fundaciobit.genapp.common.query.Where;
import org.fundaciobit.genapp.common.i18n.I18NValidationException;
import org.fundaciobit.genapp.common.web.validation.ValidationWebUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import es.caib.enviafib.back.form.webdb.*;
import es.caib.enviafib.back.form.webdb.SerieDocumentalForm;

import es.caib.enviafib.back.validator.webdb.SerieDocumentalWebValidator;

import es.caib.enviafib.persistence.SerieDocumentalJPA;
import es.caib.enviafib.model.entity.SerieDocumental;
import es.caib.enviafib.model.fields.*;

/**
 * Controller per gestionar un SerieDocumental
 *  ========= FITXER AUTOGENERAT - NO MODIFICAR !!!!! 
 * 
 * @author GenApp
 */
@Controller
@RequestMapping(value = "/webdb/serieDocumental")
@SessionAttributes(types = { SerieDocumentalForm.class, SerieDocumentalFilterForm.class })
public class SerieDocumentalController
    extends es.caib.enviafib.back.controller.EnviaFIBBaseController<SerieDocumental, java.lang.Long> implements SerieDocumentalFields {

  @EJB(mappedName = es.caib.enviafib.ejb.SerieDocumentalService.JNDI_NAME)
  protected es.caib.enviafib.ejb.SerieDocumentalService serieDocumentalEjb;

  @Autowired
  private SerieDocumentalWebValidator serieDocumentalWebValidator;

  @Autowired
  protected SerieDocumentalRefList serieDocumentalRefList;

  /**
   * Llistat de totes SerieDocumental
   */
  @RequestMapping(value = "/list", method = RequestMethod.GET)
  public String llistat(HttpServletRequest request,
    HttpServletResponse response) throws I18NException {
    SerieDocumentalFilterForm ff;
    ff = (SerieDocumentalFilterForm) request.getSession().getAttribute(getSessionAttributeFilterForm());
    int pagina = (ff == null)? 1: ff.getPage();
    return "redirect:" + getContextWeb() + "/list/" + pagina;
  }

  /**
   * Primera peticio per llistar SerieDocumental de forma paginada
   */
  @RequestMapping(value = "/list/{pagina}", method = RequestMethod.GET)
  public ModelAndView llistatPaginat(HttpServletRequest request,
    HttpServletResponse response, @PathVariable Integer pagina)
      throws I18NException {
    if(!isActiveList()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return null;
    }
    ModelAndView mav = new ModelAndView(getTileList());
    llistat(mav, request, getSerieDocumentalFilterForm(pagina, mav, request));
    return mav;
  }

  public SerieDocumentalFilterForm getSerieDocumentalFilterForm(Integer pagina, ModelAndView mav,
    HttpServletRequest request) throws I18NException {
    SerieDocumentalFilterForm serieDocumentalFilterForm;
    serieDocumentalFilterForm = (SerieDocumentalFilterForm) request.getSession().getAttribute(getSessionAttributeFilterForm());
    if(serieDocumentalFilterForm == null) {
      serieDocumentalFilterForm = new SerieDocumentalFilterForm();
      serieDocumentalFilterForm.setContexte(getContextWeb());
      serieDocumentalFilterForm.setEntityNameCode(getEntityNameCode());
      serieDocumentalFilterForm.setEntityNameCodePlural(getEntityNameCodePlural());
      serieDocumentalFilterForm.setNou(true);
    } else {
      serieDocumentalFilterForm.setNou(false);
    }
    serieDocumentalFilterForm.setPage(pagina == null ? 1 : pagina);
    return serieDocumentalFilterForm;
  }

  /**
   * Segona i següent peticions per llistar SerieDocumental de forma paginada
   * 
   * @param request
   * @param pagina
   * @param filterForm
   * @return
   * @throws I18NException
   */
  @RequestMapping(value = "/list/{pagina}", method = RequestMethod.POST)
  public ModelAndView llistatPaginat(HttpServletRequest request,
      HttpServletResponse response,@PathVariable Integer pagina,
      @ModelAttribute SerieDocumentalFilterForm filterForm) throws I18NException {
    if(!isActiveList()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return null;
    }

    ModelAndView mav = new ModelAndView(getTileList());

    filterForm.setPage(pagina == null ? 1 : pagina);
    // Actualitza el filter form

    request.getSession().setAttribute(getSessionAttributeFilterForm(), filterForm);
    filterForm = getSerieDocumentalFilterForm(pagina, mav, request);

    llistat(mav, request, filterForm);
    return mav;
  }

  /**
   * Codi centralitzat de llistat de SerieDocumental de forma paginada.
   * 
   * @param request
   * @param filterForm
   * @param pagina
   * @return
   * @throws I18NException
   */
  protected List<SerieDocumental> llistat(ModelAndView mav, HttpServletRequest request,
     SerieDocumentalFilterForm filterForm) throws I18NException {

    int pagina = filterForm.getPage();
    request.getSession().setAttribute(getSessionAttributeFilterForm(), filterForm);

    captureSearchByValueOfAdditionalFields(request, filterForm);

    preList(request, mav, filterForm);

    List<SerieDocumental> serieDocumental = processarLlistat(serieDocumentalEjb,
        filterForm, pagina, getAdditionalCondition(request), mav);

    mav.addObject("serieDocumentalItems", serieDocumental);

    mav.addObject("serieDocumentalFilterForm", filterForm);

    fillReferencesForList(filterForm,request, mav, serieDocumental, (List<GroupByItem>)mav.getModel().get("groupby_items"));

    postList(request, mav, filterForm, serieDocumental);

    return serieDocumental;
  }


  public Map<Field<?>, GroupByItem> fillReferencesForList(SerieDocumentalFilterForm filterForm,
    HttpServletRequest request, ModelAndView mav,
      List<SerieDocumental> list, List<GroupByItem> groupItems) throws I18NException {
    Map<Field<?>, GroupByItem> groupByItemsMap = new HashMap<Field<?>, GroupByItem>();
    for (GroupByItem groupByItem : groupItems) {
      groupByItemsMap.put(groupByItem.getField(),groupByItem);
    }


    return groupByItemsMap;
  }

  @RequestMapping(value = "/export/{dataExporterID}", method = RequestMethod.POST)
  public void exportList(@PathVariable("dataExporterID") String dataExporterID,
    HttpServletRequest request, HttpServletResponse response,
    SerieDocumentalFilterForm filterForm) throws Exception, I18NException {

    ModelAndView mav = new ModelAndView(getTileList());
    List<SerieDocumental> list = llistat(mav, request, filterForm);
    Field<?>[] allFields = ALL_SERIEDOCUMENTAL_FIELDS;

    java.util.Map<Field<?>, java.util.Map<String, String>> __mapping;
    __mapping = new java.util.HashMap<Field<?>, java.util.Map<String, String>>();
    exportData(request, response, dataExporterID, filterForm,
          list, allFields, __mapping, PRIMARYKEY_FIELDS);
  }



  /**
   * Carregar el formulari per un nou SerieDocumental
   */
  @RequestMapping(value = "/new", method = RequestMethod.GET)
  public ModelAndView crearSerieDocumentalGet(HttpServletRequest request,
      HttpServletResponse response) throws I18NException {

    if(!isActiveFormNew()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return null;
    }
    ModelAndView mav = new ModelAndView(getTileForm());
    SerieDocumentalForm serieDocumentalForm = getSerieDocumentalForm(null, false, request, mav);
    mav.addObject("serieDocumentalForm" ,serieDocumentalForm);
    fillReferencesForForm(serieDocumentalForm, request, mav);
  
    return mav;
  }
  
  /**
   * 
   * @return
   * @throws Exception
   */
  public SerieDocumentalForm getSerieDocumentalForm(SerieDocumentalJPA _jpa,
       boolean __isView, HttpServletRequest request, ModelAndView mav) throws I18NException {
    SerieDocumentalForm serieDocumentalForm;
    if(_jpa == null) {
      serieDocumentalForm = new SerieDocumentalForm(new SerieDocumentalJPA(), true);
    } else {
      serieDocumentalForm = new SerieDocumentalForm(_jpa, false);
      serieDocumentalForm.setView(__isView);
    }
    serieDocumentalForm.setContexte(getContextWeb());
    serieDocumentalForm.setEntityNameCode(getEntityNameCode());
    serieDocumentalForm.setEntityNameCodePlural(getEntityNameCodePlural());
    return serieDocumentalForm;
  }

  public void fillReferencesForForm(SerieDocumentalForm serieDocumentalForm,
    HttpServletRequest request, ModelAndView mav) throws I18NException {
    
  }

  /**
   * Guardar un nou SerieDocumental
   */
  @RequestMapping(value = "/new", method = RequestMethod.POST)
  public String crearSerieDocumentalPost(@ModelAttribute SerieDocumentalForm serieDocumentalForm,
      BindingResult result, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    if(!isActiveFormNew()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return null;
    }

    SerieDocumentalJPA serieDocumental = serieDocumentalForm.getSerieDocumental();

    try {
      preValidate(request, serieDocumentalForm, result);
      getWebValidator().validate(serieDocumentalForm, result);
      postValidate(request,serieDocumentalForm, result);

      if (result.hasErrors()) {
        result.reject("error.form");
        return getTileForm();
      } else {
        serieDocumental = create(request, serieDocumental);
        createMessageSuccess(request, "success.creation", serieDocumental.getSeriedocuid());
        serieDocumentalForm.setSerieDocumental(serieDocumental);
        return getRedirectWhenCreated(request, serieDocumentalForm);
      }
    } catch (Throwable __e) {
      if (__e instanceof I18NValidationException) {
        ValidationWebUtils.addFieldErrorsToBindingResult(result, (I18NValidationException)__e);
        return getTileForm();
      }
      String msg = createMessageError(request, "error.creation", null, __e);
      log.error(msg, __e);
      return getTileForm();
    }
  }

  @RequestMapping(value = "/view/{seriedocuid}", method = RequestMethod.GET)
  public ModelAndView veureSerieDocumentalGet(@PathVariable("seriedocuid") java.lang.Long seriedocuid,
      HttpServletRequest request,
      HttpServletResponse response) throws I18NException {
      return editAndViewSerieDocumentalGet(seriedocuid,
        request, response, true);
  }


  protected ModelAndView editAndViewSerieDocumentalGet(@PathVariable("seriedocuid") java.lang.Long seriedocuid,
      HttpServletRequest request,
      HttpServletResponse response, boolean __isView) throws I18NException {
    if((!__isView) && !isActiveFormEdit()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return null;
    } else {
      if(__isView && !isActiveFormView()) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return null;
      }
    }
    SerieDocumentalJPA serieDocumental = findByPrimaryKey(request, seriedocuid);

    if (serieDocumental == null) {
      createMessageWarning(request, "error.notfound", seriedocuid);
      new ModelAndView(new RedirectView(getRedirectWhenCancel(request, seriedocuid), true));
      return llistatPaginat(request, response, 1);
    } else {
      ModelAndView mav = new ModelAndView(getTileForm());
      SerieDocumentalForm serieDocumentalForm = getSerieDocumentalForm(serieDocumental, __isView, request, mav);
      serieDocumentalForm.setView(__isView);
      if(__isView) {
        serieDocumentalForm.setAllFieldsReadOnly(ALL_SERIEDOCUMENTAL_FIELDS);
        serieDocumentalForm.setSaveButtonVisible(false);
        serieDocumentalForm.setDeleteButtonVisible(false);
      }
      fillReferencesForForm(serieDocumentalForm, request, mav);
      mav.addObject("serieDocumentalForm", serieDocumentalForm);
      return mav;
    }
  }


  /**
   * Carregar el formulari per modificar un SerieDocumental existent
   */
  @RequestMapping(value = "/{seriedocuid}/edit", method = RequestMethod.GET)
  public ModelAndView editarSerieDocumentalGet(@PathVariable("seriedocuid") java.lang.Long seriedocuid,
      HttpServletRequest request,
      HttpServletResponse response) throws I18NException {
      return editAndViewSerieDocumentalGet(seriedocuid,
        request, response, false);
  }



  /**
   * Editar un SerieDocumental existent
   */
  @RequestMapping(value = "/{seriedocuid}/edit", method = RequestMethod.POST)
  public String editarSerieDocumentalPost(@ModelAttribute SerieDocumentalForm serieDocumentalForm,
      BindingResult result, SessionStatus status, HttpServletRequest request,
      HttpServletResponse response) throws I18NException {

    if(!isActiveFormEdit()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return null;
    }
    SerieDocumentalJPA serieDocumental = serieDocumentalForm.getSerieDocumental();

    try {
      preValidate(request, serieDocumentalForm, result);
      getWebValidator().validate(serieDocumentalForm, result);
      postValidate(request, serieDocumentalForm, result);

      if (result.hasErrors()) {
        result.reject("error.form");
        return getTileForm();
      } else {
        serieDocumental = update(request, serieDocumental);
        createMessageSuccess(request, "success.modification", serieDocumental.getSeriedocuid());
        status.setComplete();
        return getRedirectWhenModified(request, serieDocumentalForm, null);
      }
    } catch (Throwable __e) {
      if (__e instanceof I18NValidationException) {
        ValidationWebUtils.addFieldErrorsToBindingResult(result, (I18NValidationException)__e);
        return getTileForm();
      }
      String msg = createMessageError(request, "error.modification",
          serieDocumental.getSeriedocuid(), __e);
      log.error(msg, __e);
      return getRedirectWhenModified(request, serieDocumentalForm, __e);
    }

  }


  /**
   * Eliminar un SerieDocumental existent
   */
  @RequestMapping(value = "/{seriedocuid}/delete")
  public String eliminarSerieDocumental(@PathVariable("seriedocuid") java.lang.Long seriedocuid,
      HttpServletRequest request,HttpServletResponse response) {

    if(!isActiveDelete()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return null;
    }
    try {
      SerieDocumental serieDocumental = serieDocumentalEjb.findByPrimaryKey(seriedocuid);
      if (serieDocumental == null) {
        String __msg =createMessageError(request, "error.notfound", seriedocuid);
        return getRedirectWhenDelete(request, seriedocuid, new Exception(__msg));
      } else {
        delete(request, serieDocumental);
        createMessageSuccess(request, "success.deleted", seriedocuid);
        return getRedirectWhenDelete(request, seriedocuid,null);
      }

    } catch (Throwable e) {
      String msg = createMessageError(request, "error.deleting", seriedocuid, e);
      log.error(msg, e);
      return getRedirectWhenDelete(request, seriedocuid, e);
    }
  }


@RequestMapping(value = "/deleteSelected", method = RequestMethod.POST)
public String deleteSelected(HttpServletRequest request,
    HttpServletResponse response,
    @ModelAttribute SerieDocumentalFilterForm filterForm) throws Exception {

  if(!isActiveDelete()) {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    return null;
  }
  
  String[] seleccionats = filterForm.getSelectedItems();
  String redirect = null;
  if (seleccionats != null && seleccionats.length != 0) {
    for (int i = 0; i < seleccionats.length; i++) {
      redirect = eliminarSerieDocumental(stringToPK(seleccionats[i]), request, response);
    }
  }
  if (redirect == null) {
    redirect = getRedirectWhenDelete(request, null,null);
  }

  return redirect;
}



public java.lang.Long stringToPK(String value) {
  return java.lang.Long.parseLong(value, 10);
}

  @Override
  public String[] getArgumentsMissatge(Object __seriedocuid, Throwable e) {
    java.lang.Long seriedocuid = (java.lang.Long)__seriedocuid;
    String exceptionMsg = "";
    if (e != null) {
      if (e instanceof I18NException) {
        exceptionMsg = I18NUtils.getMessage((I18NException)e);
      } else if (e instanceof I18NValidationException) {
      } else {
        exceptionMsg = e.getMessage();
      };
    };
    if (seriedocuid == null) {
      return new String[] { I18NUtils.tradueix(getEntityNameCode()),
         getPrimaryKeyColumnsTranslated(), null, exceptionMsg };
    } else {
      return new String[] { I18NUtils.tradueix(getEntityNameCode()),
        getPrimaryKeyColumnsTranslated(),
         String.valueOf(seriedocuid),
 exceptionMsg };
    }
  }

  public String getEntityNameCode() {
    return "serieDocumental.serieDocumental";
  }

  public String getEntityNameCodePlural() {
    return "serieDocumental.serieDocumental.plural";
  }

  public String getPrimaryKeyColumnsTranslated() {
    return  I18NUtils.tradueix("serieDocumental.seriedocuid");
  }

  @InitBinder("serieDocumentalFilterForm")
  public void initBinderFilterForm(WebDataBinder binder) {
    super.initBinder(binder);
  }

  @InitBinder("serieDocumentalForm")
  public void initBinderForm(WebDataBinder binder) {
    super.initBinder(binder);

    binder.setValidator(getWebValidator());


    initDisallowedFields(binder, "serieDocumental.seriedocuid");
  }

  public SerieDocumentalWebValidator getWebValidator() {
    return serieDocumentalWebValidator;
  }


  public void setWebValidator(SerieDocumentalWebValidator __val) {
    if (__val != null) {
      this.serieDocumentalWebValidator= __val;
    }
  }


  /**
   * Entra aqui al pitjar el boto cancel en el llistat de SerieDocumental
   */
  @RequestMapping(value = "/{seriedocuid}/cancel")
  public String cancelSerieDocumental(@PathVariable("seriedocuid") java.lang.Long seriedocuid,
      HttpServletRequest request,HttpServletResponse response) {
     return getRedirectWhenCancel(request, seriedocuid);
  }

  @Override
  public String getTableModelName() {
    return _TABLE_MODEL;
  }

  // Mètodes a sobreescriure 

  public boolean isActiveList() {
    return true;
  }


  public boolean isActiveFormNew() {
    return true;
  }


  public boolean isActiveFormEdit() {
    return true;
  }


  public boolean isActiveDelete() {
    return true;
  }


  public boolean isActiveFormView() {
    return isActiveFormEdit();
  }


  public void preValidate(HttpServletRequest request,SerieDocumentalForm serieDocumentalForm , BindingResult result)  throws I18NException {
  }

  public void postValidate(HttpServletRequest request,SerieDocumentalForm serieDocumentalForm, BindingResult result)  throws I18NException {
  }

  public void preList(HttpServletRequest request, ModelAndView mav, SerieDocumentalFilterForm filterForm)  throws I18NException {
  }

  public void postList(HttpServletRequest request, ModelAndView mav, SerieDocumentalFilterForm filterForm,  List<SerieDocumental> list) throws I18NException {
  }

  public String getRedirectWhenCreated(HttpServletRequest request, SerieDocumentalForm serieDocumentalForm) {
    return "redirect:" + getContextWeb() + "/list/1";
  }

  public String getRedirectWhenModified(HttpServletRequest request, SerieDocumentalForm serieDocumentalForm, Throwable __e) {
    if (__e == null) {
      return "redirect:" + getContextWeb() + "/list";
    } else {
      return  getTileForm();
    }
  }

  public String getRedirectWhenDelete(HttpServletRequest request, java.lang.Long seriedocuid, Throwable __e) {
    return "redirect:" + getContextWeb() + "/list";
  }

  public String getRedirectWhenCancel(HttpServletRequest request, java.lang.Long seriedocuid) {
    return "redirect:" + getContextWeb() + "/list";
  }

  public String getTileForm() {
    return "serieDocumentalFormWebDB";
  }

  public String getTileList() {
    return "serieDocumentalListWebDB";
  }

  @Override
  /** Ha de ser igual que el RequestMapping de la Classe */
  public String getContextWeb() {
    RequestMapping rm = AnnotationUtils.findAnnotation(this.getClass(), RequestMapping.class);
    return rm.value()[0];
  }

  public String getSessionAttributeFilterForm() {
    return "SerieDocumentalWebDB_FilterForm";
  }



  public Where getAdditionalCondition(HttpServletRequest request) throws I18NException {
    return null;
  }


  public SerieDocumentalJPA findByPrimaryKey(HttpServletRequest request, java.lang.Long seriedocuid) throws I18NException {
    return (SerieDocumentalJPA) serieDocumentalEjb.findByPrimaryKey(seriedocuid);
  }


  public SerieDocumentalJPA create(HttpServletRequest request, SerieDocumentalJPA serieDocumental)
    throws Exception,I18NException, I18NValidationException {
    return (SerieDocumentalJPA) serieDocumentalEjb.create(serieDocumental);
  }


  public SerieDocumentalJPA update(HttpServletRequest request, SerieDocumentalJPA serieDocumental)
    throws Exception,I18NException, I18NValidationException {
    return (SerieDocumentalJPA) serieDocumentalEjb.update(serieDocumental);
  }


  public void delete(HttpServletRequest request, SerieDocumental serieDocumental) throws Exception,I18NException {
    serieDocumentalEjb.delete(serieDocumental);
  }

} // Final de Classe

