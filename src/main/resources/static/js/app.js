function ApplicationModel(map, cfg) {
    var self = this,
        //poly line reference
        trackLine = undefined,
        //marker array on poly line
        trackLineMarkers = [],
        //marker that is highlighted on map track
        trackLineHighlightMarker = null,
        //holds reference to current bounds of markers for zoom to extents
        bounds,
        //holds reference to speed/altitude chart
        speedChart;


    //public variables
    self.username = ""; //not used yet
    self.topicItems = ko.observableArray([]);
    self.topicIds = ko.observable(-1);
    self.sessionItems = ko.observableArray([]);
    self.sessionIds = ko.observable(-1);
    self.currentTrackId = -1;

    //observable rate limit - only notify when this has stopped updating for at least 500ms
    self.trackData = ko.observableArray([]).extend({ rateLimit: { timeout: 500, method: "notifyWhenChangesStop" } });

    self.init = function() {
        //initialize chart
        speedChart = new google.visualization.ChartWrapper({
            chartType: 'LineChart',
            containerId: 'speedChart',
            options: {
                vAxes: [
                    {title: 'Speed km/h', titleTextStyle: {color: 'black'}, maxValue: 10}, // Left axis
                    {title: 'Alititude m', titleTextStyle: {color: 'black'}, maxValue: 20} // Right axis
                ],
                series: [
                    {targetAxisIndex: 0},
                    {targetAxisIndex: 1}
                ],
                hAxis: {
                    viewWindow: {
                        min: 0
                    }
                },
                tooltip: { trigger: 'selection' }
            }
        });

        //wait for ready event so that we can subscript to mouse over events
        google.visualization.events.addListener(speedChart, 'ready', function() {

            google.visualization.events.addListener(speedChart.getChart(), 'select', function() {
                var chart = speedChart.getChart(),
                    selection = chart.getSelection(),
                    row = selection.length ? selection[0].row : undefined;
                if (typeof(row) !== "undefined") {
                    map.panTo(trackLineMarkers[row].getPosition());
                    highlightMarker(trackLineMarkers[row])
                } else {
                    highlightMarker(null);
                }
            });
        });

        self.topicIds.subscribe(function(newvalue) {
            getSessions(newvalue);
        });
        self.sessionIds.subscribe(function(newvalue) {
            updateTrackData(newvalue);
        });
        //kick off dom updates
        self.trackData.subscribe(function(value) {
            drawTracks(value);
            drawSpeedChart(value);
        });

        //xhr list topics
        getTopics();
    }; // end init

    self.refresh = function() {
        updateTrackData(self.currentTrackId);
    };

    self.deleteSelectedSession =  function() {
        console.log("I would delete session " + self.sessionIds());
        //deleteSession(self.sessionIds());
    };

    function deleteSession(sessionId) {
        $.ajax({
            url: '/api/sessions/delete/'+sessionId,
            dataType: 'json'
        })
            .done(function() {
                getTopics();
            })
            .fail(function() {
                alert("failed.")
            });
    }

    function getSessions(topicId) {
        self.sessionItems.removeAll();
        $.ajax({
            url: '/api/sessions/'+topicId,
            dataType: 'json'
        })
            .done(function(data) {
                $.each(data, function(i, item) {
                    self.sessionItems.push({
                        text: new Date(item.date),
                        id: item.id
                    });
                });
            })
            .fail(function() {
                alert("failed.")
            });
    }

    function getTopics() {
        self.topicItems.removeAll();
        $.ajax({
            url: '/api/topics',
            dataType: 'json'
        })
            .done(function(data) {
                $.each(data, function(i, item) {
                    self.topicItems.push({
                        text: item.name,
                        id: item.id
                    });
                });
            })
            .fail(function() {
                alert("failed.")
            });
    }

    function updateTrackData(trackId) {
        $.ajax({
            url: '/api/tracks/' + trackId,
            dataType: 'json'
        })
            .done(function(data) {
                self.currentTrackId = trackId;
                self.trackData.removeAll();
                $.each(data, function(i, item) {
                    self.trackData.push(item);
                });
            })
            .fail(function() {
                alert("failed.")
            });
    }

    //xhr to get paths for trackline, draw direction arrows, fit to bounds
    function drawTracks(tracks) {
        var path = [];
        bounds = new google.maps.LatLngBounds();
        removeTrackLine(trackLine);
        $.each(tracks, function(i, item) {
            var latLng = new google.maps.LatLng(item.lat,item.lon),
            marker = new MarkerWithLabel({
                position: latLng,
                icon: {
                    path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW,
                    rotation: item.cog,
                    scale: 2,
                    strokeColor: 'green',
                    fillColor: 'green',
                    fillOpacity: 1
                },
                _index: i
            });
            google.maps.event.addListener(marker, 'mouseover', function() {
                //index is row number of dataTable for chart
                speedChart.getChart().setSelection([{row: this._index, column:1}]);
                highlightMarker(this);
            });
            google.maps.event.addListener(marker, 'mouseout', function() {
                speedChart.getChart().setSelection(null);
                highlightMarker(null);
            });
            //keep reference to each marker
            trackLineMarkers.push(marker);
            //keep array of latlngs for draw line
            path.push(latLng);
            //push bounds for each latlng to ensure map view port contains marker
            bounds.extend(latLng);
        });
        //draw line
        drawTrackLine(path);
        //ensure map contains all markers
        map.fitBounds(bounds);
    }

    //draw highlight around marker
    function highlightMarker(marker) {
        function doHighLight(m) {
            if (m) {
                var icon = m.getIcon();
                icon.scale = icon.scale == 2 ? 4 : 2;
                icon.strokeColor = icon.strokeColor == 'green' ? 'yellow' : 'green'
                m.setIcon(icon);
            }
            return m;
        }
        doHighLight(trackLineHighlightMarker);
        trackLineHighlightMarker = doHighLight(marker);
    }

    //draw trackline on map
    function drawTrackLine(path) {
        trackLine  = new google.maps.Polyline({
            path: path,
            strokeColor: 'green',
            fillColor: 'green',
            fillOpacity: .7,
            strokeWeight: 3,
            strokeOpacity:.65
        });
        $.each(trackLineMarkers, function(i,item) {
           item.setMap(map);
        });
        trackLine.setMap(map);
    }

    //remove track line from map
    function removeTrackLine(path) {
        if (trackLine) {
            trackLine.setMap(null);
        }
        $.each(trackLineMarkers, function(i,item) {
           item.setMap(null);
        });
        trackLineMarkers = [];
    }

    /**
     * Draw a chart plotting speed, time, altitude
     * @param el
     * @param trackId
     */

    function drawSpeedChart(trackData) {
        var dataTable = new google.visualization.DataTable(),
            monthYearFormatter,
            startTime = 0;
        //TODO: fix this dependency
        //WARNING: dont forget to change columns indexes in hover over if you much wiht columns
        //tooltip must follow applicable column
        dataTable.addColumn('number', 'Date');
        dataTable.addColumn('number', 'Speed');
        dataTable.addColumn({type: 'string', role: 'tooltip'});
        dataTable.addColumn('number', 'Altitude');
        dataTable.addColumn({type: 'string', role: 'tooltip'});
        //add lat/lon for hover over event markers on map
        dataTable.addColumn('number', 'lat');
        dataTable.addColumn('number', 'lon');


        $.each(trackData, function (i, item) {
            if (i == 0) startTime = item.tst;
            var minutes = (item.tst - startTime) / 60;
            dataTable.addRow([ minutes , item.vel, getSpeedChartToolTip(item, {minutes: minutes}), item.alt, getSpeedChartToolTip(item, {minutes: minutes}), item.lat, item.lon])

        });

        speedChart.setDataTable(dataTable);
        speedChart.setView({
            columns: [0,1,2,3,4]
        });
        speedChart.draw();
    }

    //TODO: replace with HTML tooltip
    function getSpeedChartToolTip(item,extra) {
        return  "Date: " + new Date(item.tst*1000) + "\n" +
                "Time: " + Math.floor(extra.minutes) + "\n" +
                "Velocity: " + item.vel + "km/h (" + Math.floor(item.vel * .621371) + " mph)\n" +
                "Altitude: " + item.alt + "m (" + Math.floor(item.alt * 3.28084) +  "ft)";
    }

}