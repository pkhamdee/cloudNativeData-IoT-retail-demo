var numMessages;
var messageJSON;

$(document).ready(
    function() {
        //sse = new EventSource('/live_tweets'); //GG
    	sse = new EventSource('live_alerts');
        sse.onmessage = function(message) {
        	
        	//console.log("data: "+message.data);
        	var newMessage= JSON.parse(message.data);
        	var tableContent = document.getElementById("output");
            
        	if(tableContent.innerHTML.indexOf(newMessage.body) > -1)
        		return false;
        	
        	if (numMessages > 10) {
                document.getElementById("output").deleteRow(-1);
            }
        	
               
                $('#output').prepend('<tr id="tweet-row"> ' +
                '<td id="tweet-cell" class="col-sm-10">&nbsp<div class="verticalLine">'+ urlify(newMessage.body) + '</div></td>' +
                '<td id="sentiment-cell" class="col-sm-2">&nbsp;</td> </tr>');
                //'<td id="sentiment-cell" class="col-sm-2">' + polarityToLabel(messageJSON.polarity) + '</td> </tr>');

                numMessages++;
        }
        sse.onerror = function(error) {
        	
        	console.log("error: "+error);
        }
    }
);
function polarityToLabel(p) {
    if (p >= 0.9) {
        return "<div style=\"color:green\"> pos </div> "
    } else if (p <= 0.1) {
        return "<div style=\"color:red\"> neg </div> "
    }
    return "neu"
}

function urlify(text) {
    // source: http://stackoverflow.com/questions/1500260/detect-urls-in-text-with-javascript
    var urlRegex = /(https?:\/\/[^\s]+)/g;
    return text.replace(urlRegex, function(url) {
        return '<a href="' + url + '">' + url + '</a>';
    })
}

