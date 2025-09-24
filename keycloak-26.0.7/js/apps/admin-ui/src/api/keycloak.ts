// src/api/keycloak.ts
import axios from 'axios';

export const updateRealmAttributes = async (
  realm: string,
  attributes: Record<string, string>
) => {
  const token = await getAdminToken(); // 管理者トークン取得（別途定義）

  const realmConfig = await axios.get(`${KEYCLOAK_URL}/admin/realms/${realm}`, {
    headers: { Authorization: `Bearer ${token}` },
  });

  const updatedConfig = {
    ...realmConfig.data,
    attributes: {
      ...realmConfig.data.attributes,
      ...attributes,
    },
  };

  await axios.put(`${KEYCLOAK_URL}/admin/realms/${realm}`, updatedConfig, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });
};