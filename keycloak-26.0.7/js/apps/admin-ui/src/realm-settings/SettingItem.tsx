import React from 'react';

export interface SettingItemProps {
  id: string;
  name: string;
  riskScore: number;
  enabled: boolean;
  onChange: (enabled: boolean) => void;
}

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

const SettingItems: React.FC<SettingItemProps> = ({ name, riskScore, enabled, onChange }) => {
  const riskLabel = getRiskLabel(riskScore);
  const riskColor = getRiskColor(riskScore);

  return (
    <div style={{ border: `2px solid ${riskColor}`, padding: '1rem', marginBottom: '1rem' }}>
      <h3>{name}</h3>
      <p>
        <strong>リスクスコア:</strong>{' '}
        <span style={{ color: riskColor }}>{riskScore} / 100（{riskLabel}）</span>
      </p>
      <label>
        <input
          type="checkbox"
          checked={enabled}
          onChange={(e) => onChange(e.target.checked)}
        />
        有効化
      </label>
    </div>
  );
};

export default SettingItem;