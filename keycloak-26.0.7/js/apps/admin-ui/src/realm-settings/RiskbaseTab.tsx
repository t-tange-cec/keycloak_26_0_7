import type RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";
import {
  HelpItem,
  KeycloakSelect,
  SelectVariant,
  SwitchControl,
} from "@keycloak/keycloak-ui-shared";
import {
  ActionGroup,
  Button,
  FormGroup,
  PageSection,
  SelectOption,
} from "@patternfly/react-core";
import { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { FormAccess } from "../components/form/FormAccess";
import { useServerInfo } from "../context/server-info/ServerInfoProvider";
import { convertToFormValues } from "../util";

import { useConfirmDialog } from "../components/confirm-dialog/ConfirmDialog";

type RealmSettingsThemesTabProps = {
  realm: RealmRepresentation;
  save: (realm: RealmRepresentation) => void;
};


export const RiskbaseTab = ({
	  realm,
	  save,
	}: RealmSettingsThemesTabProps) => {
	const { t } = useTranslation();
	const { control, handleSubmit, setValue } = useForm<RealmRepresentation>();
	const setupForm = () => {
	  convertToFormValues(realm, setValue);
	};
	useEffect(setupForm, []);
	const riskbaseEnabled = useWatch({
	  control,
	  name: "riskbaseEnabled",
	  defaultValue: realm.riskbaseEnabled,
	});

	return (
		<PageSection variant="light">
		<FormAccess
		  isHorizontal
		  role="manage-realm"
		  className="pf-v5-u-mt-lg"
		  onSubmit={handleSubmit(save)}
		>
		
		<FormProvider {...form}>
		  <SwitchControl
		    name="riskbaseEnabled"
		    label={t("riskbase")}
		    labelIcon={t("riskbaseHelp")}
		    labelOn={t("enabled")}
		    labelOff={t("disabled")}
		    aria-label={t("riskbase")}
		  />
		</FormProvider>
		<ActionGroup>
		  <Button variant="primary" type="submit" data-testid="riskbase-tab-save">
		    {t("save")}
		  </Button>
		  <Button variant="link" onClick={setupForm}>
		    {t("revert")}
		  </Button>
		</ActionGroup>
		</FormAccess>
		</PageSection>
	);
};
