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
	const questionData = [
	  { id: 'PI001', jp: "\u3042\u306a\u305f\u306e\u597d\u304d\u306a\u6620\u753b\u306f\uff1f", en: "What's your favorite movie?" },
	  { id: 'PI002', jp: "\u3042\u306a\u305f\u306e\u30da\u30c3\u30c8\u306e\u540d\u524d\u306f\uff1f", en: "What's your pet's name?" },
	  { id: 'PI003', jp: "\u3042\u306a\u305f\u306e\u6bcd\u89aa\u306e\u65e7\u59d3\u306f\uff1f", en: "What's your mother's maiden name?" },
	  { id: 'PI004', jp: "\u3042\u306a\u305f\u306e\u51fa\u8eab\u5730\u306f\uff1f", en: "Where are you from?" },
	  { id: 'PI005', jp: "\u3042\u306a\u305f\u306e\u597d\u304d\u306a\u30b9\u30dd\u30fc\u30c4\u30c1\u30fc\u30e0\u306f\uff1f", en: "What's your favorite sports team?" },
	  { id: 'PI006', jp: "\u521d\u3081\u3066\u65c5\u884c\u3057\u305f\u5834\u6240\u306f\uff1f", en: "Where was the first place you traveled to?" },
	];
	const [questions, setQuestions] = useState(questionData);
	const onSubmit = (formData: RealmRepresentation) => {
	  formData.attributes = {
	    ...formData.attributes,
	    secretQuestions: JSON.stringify(questions),
	  };
	  save(formData);
	};
	const setupForm = () => {
	  convertToFormValues(realm, setValue);
	  const saved = realm.attributes?.secretQuestions;
	  if (saved) {
	    try {
	      const parsed = JSON.parse(saved);
	      if (Array.isArray(parsed)) {
	        setQuestions(parsed);
	      }
	    } catch (e) {
	      console.error("”é–§‚Ì¿–â‚Ì“Ç‚İ‚İ‚É¸”s‚µ‚Ü‚µ‚½", e);
	    }
	  }
	};
	
	useEffect(setupForm, []);
	
	return (
		<PageSection variant="light">
		<FormAccess
		  isHorizontal
		  role="manage-realm"
		  className="pf-v5-u-mt-lg"
		  onSubmit={handleSubmit(onSubmit)}
		>
		<Button onClick={() => setQuestions([...questions, { id: `PI${String(questions.length + 1).padStart(3, '0')}`, jp: "", en: "" }])}>
		{t("add")}
		</Button>
		<div>
		   <h2>”é–§‚Ì¿–âˆê——</h2>
		   <table>
		     <thead>
		       <tr>
		         <th>ID</th>
		         <th>jp</th>
		         <th>en</th>
		       </tr>
		     </thead>
			 <tbody>
			   {questions.map((q, index) => (
			     <tr key={q.id}>
			       <td>{q.id}</td>
			       <td>
			         <input
			           type="text"
			           value={q.jp}
			           onChange={(e) => {
			             const updated = [...questions];
			             updated[index].jp = e.target.value;
			             setQuestions(updated);
			           }}
			         />
			       </td>
			       <td>
			         <input
			           type="text"
			           value={q.en}
			           onChange={(e) => {
			             const updated = [...questions];
			             updated[index].en = e.target.value;
			             setQuestions(updated);
			           }}
			         />
			       </td>
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
