import type RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";
import React from 'react';
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
	const questionData = [
	  { id: 'PI001', jp: 'あなたの好きな映画は？', en: "What's your favorite movie?" },
	  { id: 'PI002', jp: 'あなたのペットの名前は？', en: "What's your pet's name?" },
	  { id: 'PI003', jp: 'あなたの母親の旧姓は？', en: "What's your mother's maiden name?" },
	  { id: 'PI004', jp: 'あなたの出身地は？', en: "Where are you from?" },
	  { id: 'PI005', jp: 'あなたの好きなスポーツチームは？', en: "What's your favorite sports team?" },
	  { id: 'PI006', jp: '初めて旅行した場所は？', en: "Where was the first place you traveled to?" },
	];
	
	useEffect(setupForm, []);
	return (
		<PageSection variant="light">
		<FormAccess
		  isHorizontal
		  role="manage-realm"
		  className="pf-v5-u-mt-lg"
		  onSubmit={handleSubmit(save)}
		>
		<div>
		   <h2>秘密の質問一覧</h2>
		   <table>
		     <thead>
		       <tr>
		         <th>ID</th>
		         <th>質問(日本語)</th>
		         <th>Question (English)</th>
		       </tr>
		     </thead>
		     <tbody>
		       {questionData.map((q) => (
		         <tr key={q.id}>
		           <td>{q.id}</td>
		           <td>{q.jp}</td>
		           <td>{q.en}</td>
		         </tr>
		       ))}
		     </tbody>
		   </table>
		 </div>
		
		<ActionGroup>
		  <Button variant="primary" type="submit" data-testid="secret-tab-save">
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
