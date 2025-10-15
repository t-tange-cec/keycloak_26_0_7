import type RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";
import React from 'react';
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
import SettingItem from './SettingItem'; 


type RealmSettingsThemesTabProps = {
  realm: RealmRepresentation;
  save: (realm: RealmRepresentation) => void;
};

export interface SettingItemProps{
	id: string;
	name: string;
  riskScore: number; // 0〜100のスコア
  onChange: (enabled: boolean) => void;
  enabled: boolean;
};

export const settings: SettingItemProps[] = [
  {
    id: '0',
    name: '認証失敗チェック',
    riskScore: 20,
    enabled: true,
    onChange: (enabled) => {
      console.log(`設定 認証失敗チェック が ${enabled ? '有効化' : '無効化'} されました`);
    },
  },
  {
    id: '1',
    name: 'IP履歴チェック',
    riskScore: 30,
    enabled: true,
    onChange: (enabled) => {
      console.log(`設定 IP履歴チェック が ${enabled ? '有効化' : '無効化'} されました`);
    },
  },
  {
    id: '2',
    name: '最終ログインからの経過時間チェック',
    riskScore: 30,
    enabled: true,
    onChange: (enabled) => {
      console.log(`設定 最終ログインからの経過時間チェック が ${enabled ? '有効化' : '無効化'} されました`);
    },
  },
];


const getRiskLabel = (score: number): string => {
  if (score < 30) return '低';
  if (score < 70) return '中';
  return '高';
};

const getRiskColor = (score: number): string => {
  if (score < 30) return 'green';
  if (score < 70) return 'orange';
  return 'red';
};

export const RiskbaseTab = ({
	  realm,
	  save,
	}: RealmSettingsThemesTabProps) => {
	const form = useForm();
	const { control, handleSubmit, setValue } = form;
	const { t } = useTranslation();
	const [settingsState, setSettingsState] = useState(settings);
	const handleToggle = (id: string, newEnabled: boolean) => {
	  setSettingsState((prev) =>
	    prev.map((item) =>
	      item.id === id ? { ...item, enabled: newEnabled } : item
	    )
	  );
	};
	
	const threshhold = useWatch({
	  control,
	  name: "threshhold",
	  defaultValue: 50,
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
	const handleSave = async (formValues: any) => {
	  const updatedRealm: RealmRepresentation = {
	    ...realm,
	    attributes: {
	      ...realm.attributes,
	      EnableRiskbase: formValues.riskbaseEnabled ? "true" : "false",
	    },
	  };

	  save(updatedRealm);
	  alert("属性を更新しました");
	};
	return (
		<PageSection variant="light">
		<FormAccess
		  isHorizontal
		  role="manage-realm"
		  className="pf-v5-u-mt-lg"
		  onSubmit={handleSubmit(handleSave)}
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
		<div>
		  <h2>🔧 管理設定一覧</h2>
		  {settings.map((setting) => (
		    <SettingItem
		      key={setting.id}
		      id={setting.id}
		      name={setting.name}
		      riskScore={setting.riskScore}
		      enabled={setting.enabled}
		      onChange={(newEnabled) => handleToggle(setting.id, newEnabled)}
		    />
			))}
		</div>
		
		<h2>t("threshhold")</h2>
		<input
		  type="text"
		  value={threshhold}
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
