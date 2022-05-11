---
--- 09/05/2022 - Crear columnes de gestió de fitxers firmats i resposta de PortaFIB a la taula Peticio #11
---
ALTER TABLE efi_peticio
  ADD COLUMN estat smallint NOT NULL DEFAULT 0;
ALTER TABLE efi_peticio
  ADD COLUMN fitxer_firmatid bigint;

ALTER TABLE efi_peticio
  ADD CONSTRAINT efi_peticio_fitxer_ffirm_fk FOREIGN KEY (fitxer_firmatid) REFERENCES efi_fitxer (fitxerid) ON UPDATE NO ACTION ON DELETE NO ACTION;




---
--- 11/05/2022 - Guardar a Petició el ID de petició intern de PortaFIB. #15
---

ALTER TABLE efi_peticio
  ADD COLUMN portafibid bigint;
COMMENT ON COLUMN efi_peticio.portafibid IS 'Id de la peticio dins el sistema de portafib un cop enviada.';
