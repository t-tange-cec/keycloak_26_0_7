import type RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";
import {
  HelpItem,
  KeycloakSelect,
  SelectVariant,
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

export const SecretTab = ({
	  realm,
	  save,
	}: RealmSettingsThemesTabProps) => {
	const { t } = useTranslation();
	const { control, handleSubmit, setValue } = useForm<RealmRepresentation>();
	const setupForm = () => {
	  convertToFormValues(realm, setValue);
	};
	useEffect(setupForm, []);
	return (
		<PageSection variant="light">
		<div>This is SecretTab</div>
		</PageSection>		
	);
};
