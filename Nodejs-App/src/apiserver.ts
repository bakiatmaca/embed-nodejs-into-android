import http from "http";
import { platform } from 'node:process';
const os = require("os");


class ApiServer {
  port: string;
  server: any;

  constructor(port: string) {
    this.port = port;
  }

  listen() {
    this.server?.listen(this.port, () => {
      console.log(`ApiServer listen port ${this.port}`);
     });
  }

  create() {
    this.server = http.createServer((req, res) => {
      
      // get
      if (req.method == "GET" && req.url == "/api/get") {
        res.writeHead(200, { "Content-Type": "application/json" });
        res.end(
          JSON.stringify({
            visit: "http://bakiatmaca.com",
            details:"http://bakiatmaca.com/nodejs-runtime-android-uygulamaya-gommek-ve-gradle-build-script-hazirlama-d5e6621aab3c",
            message: `Node.js says Hello from ${platform}`,
            OsVer: os.release(),
          })
        );
      }
   
      // Post
      if (req.method == "POST" && req.url == "/api/set") {

        res.writeHead(200, { "Content-Type": "application/json" });
        res.end(
          JSON.stringify({
            success: true,
            message: "POST is done",
          })
        ); 
      }
   
   });
  }
}

export { ApiServer };
