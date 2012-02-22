var data = [];

function pushReading(reading) {
	data.push([Date.parse(reading.date), reading.watts]);
}

$(function() {
	$.getJSON('/history', function(d) {
		$.each(d, function(key, value) {
			pushReading(value);
		});

		var plot = $.plot($('#usagegraph'), [ data ], {
			series: { shadowSize: 0 },
			yaxis: { min: 0 },
			xaxis: { mode: 'time' },
		});

		var ws = new WebSocket('ws://' + window.location.host + '/data');
		ws.onopen = function() {
			var status = $('#status>span');
			
			status.text('Connected');
			status.removeClass().addClass('connected');
		}

		ws.onmessage = function(e) {
			var reading = JSON.parse(e.data);

			$('#currentreading>span').text(reading.watts);
			$('#latestreading>span').text(reading.date);

			pushReading(reading);

			plot.setData([ data ]);
			plot.setupGrid();
			plot.draw();
		};

		ws.onclose = function() {
			var status = $('#status>span');
			
			status.text('Disconnected');
			status.removeClass().addClass('disconnected');
		}
	});
});
