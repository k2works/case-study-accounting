import { config } from './config';

export const App = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>{config.appName}</h1>
      <p>フロントエンド環境が正常に構築されました。</p>
      <p>開発モード: {config.isDev ? 'ON' : 'OFF'}</p>
    </div>
  );
};
