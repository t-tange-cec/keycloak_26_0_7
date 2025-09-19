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
import { useTranslation } from "react-i18next";
import { useConfirmDialog } from "../components/confirm-dialog/ConfirmDialog";

export const SecretTab = () => {
	const { t } = useTranslation();
	const save = async () => {
	    return;
	};
	return (
		<PageSection variant="light">
		<div>This is SecretTab</div>
		</PageSection>		
	);
};
