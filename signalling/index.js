"use strict";

// Node Get ICE STUN and TURN list
// let o = {
//   format: "urls"
// };

// let bodyString = JSON.stringify(o);
// let https = require("https");
// let options = {
//   host: "global.xirsys.net",
//   path: "/_turn/MyFirstApp",
//   method: "PUT",
//   headers: {
//       "Authorization": "Basic " + Buffer.from("lotus9492:efb4281c-eb67-11ea-91cd-0242ac150003").toString("base64"),
//       "Content-Type": "application/json",
//       "Content-Length": bodyString.length
//   }
// };
// let httpreq = https.request(options, function(httpres) {
//   let str = "";
//   httpres.on("data", function(data){ str += data; });
//   httpres.on("error", function(e){ console.log("error: ",e); });
//   httpres.on("end", function(){ 
//       console.log("ICE List: ", str);
//   });
// });
// httpreq.on("error", function(e){ console.log("request error: ",e); });
// httpreq.end(bodyString);

var os = require("os");
var nodeStatic = require("node-static");
var fileServer = new nodeStatic.Server();
var http = require('http');

var app = http.createServer(function(req, res){
    req.addListener('end', function(){
        fileServer.serve(req, res, function(err, result){
            if (err) { // There was an error serving the file
                console.error("Error serving " + req.url + " - " + err.message);
 
                // Respond to the client
                res.writeHead(err.status, err.headers);
                res.end();
            }
        });
    }).resume();
}).listen(3000);

var io = require('socket.io')(app);

io.sockets.on('connection', function(socket) {

    console.log("connected");

    // convenience function to log server messages on the client
    function log() {
      var array = ['Message from server:'];
      array.push.apply(array, arguments);
      socket.emit('log', array);
    }
  
    socket.on('message', function(message) {
      log('Client said: ', message);
      // for a real app, would be room-only (not broadcast)
      socket.broadcast.emit('message', message);
    });
  
    socket.on('create or join', function(room) {
      log('Received request to create or join room ' + room);
  
      var numClients = io.sockets.sockets.length;
      log('Room ' + room + ' now has ' + numClients + ' client(s)');
  
      if (numClients === 1) {
        socket.join(room);
        log('Client ID ' + socket.id + ' created room ' + room);
        socket.emit('created', room, socket.id);
  
      } else if (numClients === 2) {
        log('Client ID ' + socket.id + ' joined room ' + room);
        io.sockets.in(room).emit('join', room);
        socket.join(room);
        socket.emit('joined', room, socket.id);
        io.sockets.in(room).emit('ready');
      } else { // max 5 clients
        socket.emit('full', room);
      }
    });
  
    socket.on('ipaddr', function() {
      var ifaces = os.networkInterfaces();
      for (var dev in ifaces) {
        ifaces[dev].forEach(function(details) {
          if (details.family === 'IPv4' && details.address !== '127.0.0.1') {
            socket.emit('ipaddr', details.address);
          }
        });
      }
    });
  
    socket.on('bye', function() {
      console.log('received bye');
    });
  });
  