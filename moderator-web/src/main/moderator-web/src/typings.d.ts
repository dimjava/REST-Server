/* SystemJS module definition */

declare var require: NodeRequire;

interface NodeRequireFunction {
  (id: string): any;
}

interface NodeRequire extends NodeRequireFunction {
  resolve(id: string): string;
  cache: any;
  extensions: any;
  main: NodeModule | undefined;
}

declare var module: NodeModule;
interface NodeModule {
  id: string;
}
