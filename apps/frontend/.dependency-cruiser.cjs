/** @type {import('dependency-cruiser').IConfiguration} */
module.exports = {
  forbidden: [
    {
      name: 'no-circular',
      severity: 'error',
      comment: '循環参照を禁止します',
      from: {},
      to: {
        circular: true,
      },
    },
    {
      name: 'no-orphans',
      severity: 'warn',
      comment: '使用されていないファイルを警告します',
      from: {
        orphan: true,
        pathNot: [
          '(^|/)\\.[^/]+\\.(js|cjs|mjs|ts|json)$', // ドットファイル
          '\\.d\\.ts$', // 型定義ファイル
          '(^|/)tsconfig\\.json$', // tsconfig
          '(^|/)(babel|webpack|vite)\\.config\\.(js|cjs|mjs|ts|json)$', // 設定ファイル
          '(^|/)main\\.tsx?$', // エントリポイント
          '(^|/)test/.*$', // テストセットアップ
          '(^|/)mocks/.*$', // MSW モック
        ],
      },
      to: {},
    },
    {
      name: 'not-to-deprecated',
      comment: '非推奨のモジュールへの依存を警告します',
      severity: 'warn',
      from: {},
      to: {
        dependencyTypes: ['deprecated'],
      },
    },
    {
      name: 'no-non-package-json',
      severity: 'error',
      comment: 'package.json に記載されていないパッケージへの依存を禁止します',
      from: {},
      to: {
        dependencyTypes: ['npm-no-pkg', 'npm-unknown'],
      },
    },
  ],
  options: {
    doNotFollow: {
      path: 'node_modules',
    },
    tsPreCompilationDeps: true,
    tsConfig: {
      fileName: 'tsconfig.json',
    },
    enhancedResolveOptions: {
      exportsFields: ['exports'],
      conditionNames: ['import', 'require', 'node', 'default'],
    },
    reporterOptions: {
      dot: {
        collapsePattern: 'node_modules/[^/]+',
      },
      archi: {
        collapsePattern: '^(node_modules|packages|src|lib|app|test)/[^/]+',
      },
    },
    exclude: {
      path: ['node_modules', 'dist', 'coverage', 'src/api/generated', 'src/api/model'],
    },
  },
};
