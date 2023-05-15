getDiscrim(Discrim) :- Discrim >= 0.

getName(Ref, Return) :- 
    jpl_call(Ref, getName, [], Return).

%Radar
radarTurn(RHeading, EBearing, RRadarHeading, Res) :- Res is (RHeading + EBearing) - RRadarHeading.

turnRadar(Rref, Eref) :-
    getHeadingRadians(Rref, Heading),
    getBearingRadians(Eref, Bearing),
    AbsBearing is Heading + Bearing,
    getRadarHeadingRadians(Rref, RadarHeading),
    NewAbsBearing is AbsBearing - RadarHeading,
    normalRelativeAngle(NewAbsBearing, NewTurnAmount),
    setTurnRadarRightRadians(Rref, NewTurnAmount).

getHeadingRadians(Ref, Return) :-
    jpl_call(Ref, getHeadingRadians, [], Return).

getBearingRadians(Ref, Return) :-
    jpl_call(Ref, getBearingRadians, [], Return).

getRadarHeadingRadians(Ref, Return) :-
    jpl_call(Ref, getRadarHeadingRadians, [], Return).

normalRelativeAngle(Amount, Return) :- 
	jpl_call('robocode.util.Utils', normalRelativeAngle, [Amount], Return).

setTurnRadarRightRadians(Ref, Amount) :- 
    jpl_call(Ref, setTurnRadarRightRadians, [Amount], _).

%Movimentação
moveRobot(ERef, Ref, Distance) :- 
    getBearingRadians(ERef, Bearing),
    Amount is cos(Bearing),
    setTurnRadarRightRadians(Ref, Amount),
    setAhead(Ref, Distance).

setAhead(Ref, Distance) :-
    jpl_call(Ref, setAhead, [Distance], _).