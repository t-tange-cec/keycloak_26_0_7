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
import { Controller, FormProvider, useForm, useWatch } from "react-hook-form";
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
	const form = useForm();
	const { control, handleSubmit, setValue } = form;
	const { t } = useTranslation();
	const threshhold_value = useState('50');
	const threshhold = useWatch({
	  control,
	  name: "threshhold",
	  defaultValue: threshhold_value,
	});
	const setupForm = () => {
	  convertToFormValues(realm, setValue);
	};
	useEffect(setupForm, []);
	const riskbaseEnabled = useWatch({
	  control,
	  name: "riskbaseEnabled",
	  defaultValue: false,
	});
	const handleSave = async () => {
	  alert('属性を更新しました');
	};

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
		<h2>スレッシュホールド</h2>
		<input
		  type="text"
		  value={threshhold}
		  onChange={(e) => setValue(e.target.value)}
		  placeholder="threshhold を入力"
		/>
		
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
