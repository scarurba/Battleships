
var miInit = { method: 'GET',
               cache: 'default'
               };
fetch('/api/games',miInit)
.then(function(response) {
   return  response.json();
})
.then(function(data) {
   console.log(data)
  var htmlOL = document.getElementById("games");
 data.map(element => {
     var item = document.createElement("li");
     item.appendChild(document.createTextNode(element.id +"    "+ new Date(element.created).toLocaleString()  +" "+   element.gamePlayers[0].player.email +" VS "+    element.gamePlayers[1].player.email));
    document.body.insertBefore(item, htmlOL);
 });
});
$("#table-leaderboard").append(
  '<tr>'
  + '<td>Name</td>'
  + '<td>' + stats.numberD + '</td>'
  + '</tr>'
  + '<tr>'
  + '<td>Total</td>'
  + '<td>' + stats.numberR + '</td>'
  + '<td>' + stats.VWPR + '</td>'
  + '</tr>'
  + '<td>Won</td>'
  + '<td>' + stats.numberI + '</td>'
  + '<td>' + stats.VWPI + '</td>'
  + '</tr>'
  + '<td>Total</td>'
  + '<td>' + stats.total + '</td>'
  + '<td>' + stats.VWPTotal + '</td>'
  + '</tr>'
);