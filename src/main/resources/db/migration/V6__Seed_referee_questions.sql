-- ======== SEED: 50 Pickleball Referee Test Questions (10 per category) ========

-- ==================== SCORING (10 questions) ====================
INSERT INTO referee_test_questions (category, question_text, option_a, option_b, option_c, option_d, correct_answer, difficulty) VALUES
('SCORING', 'In a standard Pickleball game, what is the winning score?', '15 points', '11 points', '21 points', '25 points', 'B', 'EASY'),
('SCORING', 'By how many points must a team win in Pickleball?', '1 point', '2 points', '3 points', 'No minimum margin', 'B', 'EASY'),
('SCORING', 'When can the serving team score a point?', 'Only when serving', 'Only when receiving', 'Both serving and receiving', 'During a rally only', 'A', 'EASY'),
('SCORING', 'In doubles, what is the starting score announced at the beginning of each game?', '0-0-1', '0-0-2', '0-0-0', '1-0-1', 'B', 'MEDIUM'),
('SCORING', 'What does the third number in a doubles score represent?', 'The game number', 'The server number (1 or 2)', 'The number of faults', 'The rally count', 'B', 'MEDIUM'),
('SCORING', 'If the score is 7-5-2, what does this mean?', 'Serving team has 7, receiving has 5, server 2', 'Game 7, set 5, point 2', 'Player 7 serves to player 5, attempt 2', '7th rally, 5th fault, 2nd timeout', 'A', 'MEDIUM'),
('SCORING', 'In singles Pickleball, how many numbers are called for the score?', '1', '2', '3', '4', 'B', 'EASY'),
('SCORING', 'When a side-out occurs in doubles, what happens to the score?', 'Score resets to zero', 'Serving team loses a point', 'The serve passes to the other team', 'The game ends', 'C', 'MEDIUM'),
('SCORING', 'In tournament play, some games are played to what score?', '11 or 15 points', '21 or 25 points', '7 or 9 points', '30 points', 'A', 'HARD'),
('SCORING', 'At the start of a doubles game, how many serves does the first serving team get before a side-out?', '1', '2', '3', '0', 'A', 'HARD');

-- ==================== FAULT (10 questions) ====================
INSERT INTO referee_test_questions (category, question_text, option_a, option_b, option_c, option_d, correct_answer, difficulty) VALUES
('FAULT', 'Which of the following is a fault in Pickleball?', 'Hitting the ball into the net', 'Hitting the ball deep but in bounds', 'A volley from behind the baseline', 'A drop shot that lands in the kitchen', 'A', 'EASY'),
('FAULT', 'What happens when a fault is committed by the receiving team?', 'The serving team scores a point', 'The serve passes to the other team', 'A let is called', 'The rally is replayed', 'A', 'EASY'),
('FAULT', 'Is it a fault if the ball bounces twice on one side before being returned?', 'Yes, always', 'No, never', 'Only in singles', 'Only in tournament play', 'A', 'EASY'),
('FAULT', 'A player hits a volley while standing in the Non-Volley Zone. Is this a fault?', 'Yes', 'No', 'Only if the ball was above the net', 'Only on the serve', 'A', 'MEDIUM'),
('FAULT', 'If a player''s momentum carries them into the NVZ after a volley, is it a fault?', 'Yes', 'No', 'Only if they touch the net', 'Only in doubles', 'A', 'MEDIUM'),
('FAULT', 'What happens when a fault is committed by the serving team in doubles?', 'The serve moves to the second server or side-out', 'They automatically lose the game', 'The other team scores a point', 'A timeout is called', 'A', 'MEDIUM'),
('FAULT', 'Is it a fault if the served ball hits the net and lands in the correct service area?', 'No, it is a let and replayed', 'Yes, it is always a fault', 'Only in tournament play', 'No, the serve continues', 'A', 'HARD'),
('FAULT', 'A player catches the ball before it goes out of bounds. Is this a fault?', 'Yes, the ball must bounce or be let go', 'No, it saves time', 'Only if the opponent objects', 'Only on the serve return', 'A', 'HARD'),
('FAULT', 'If a player hits the ball and it touches any part of their body before going over the net, is it a fault?', 'Yes', 'No', 'Only if it touches their hand', 'Only in doubles', 'A', 'MEDIUM'),
('FAULT', 'Is it a fault if the ball hits the ceiling during indoor play?', 'Yes', 'No', 'Only if it hits the lights', 'Depends on house rules', 'A', 'EASY');

-- ==================== LINE_CALL (10 questions) ====================
INSERT INTO referee_test_questions (category, question_text, option_a, option_b, option_c, option_d, correct_answer, difficulty) VALUES
('LINE_CALL', 'If the ball lands on the line during a rally, is it in or out?', 'In', 'Out', 'Replay the point', 'Depends on which line', 'A', 'EASY'),
('LINE_CALL', 'During a serve, if the ball lands on the Non-Volley Zone line (kitchen line), is the serve good?', 'No, it is a fault', 'Yes, it is good', 'It is a let', 'Only in singles', 'A', 'MEDIUM'),
('LINE_CALL', 'Who makes the line call on their side of the court?', 'The referee only', 'The player(s) on that side', 'The serving team always', 'The spectators', 'B', 'EASY'),
('LINE_CALL', 'If there is a disagreement on a line call in non-officiated play, what should happen?', 'The point goes to the serving team', 'The ball is called in (benefit of doubt)', 'The point is replayed', 'The receiving team decides', 'B', 'MEDIUM'),
('LINE_CALL', 'In officiated tournament play, who has the final say on line calls?', 'The players', 'The referee', 'The tournament director', 'The spectators', 'B', 'MEDIUM'),
('LINE_CALL', 'If the ball clips the top of the net and lands in the correct area during a rally, what is the call?', 'Let, replay the point', 'The ball is in play and continues', 'Fault on the hitter', 'Side-out', 'B', 'MEDIUM'),
('LINE_CALL', 'A ball lands on the baseline. What is the correct call?', 'Out', 'In', 'Let', 'Depends on the speed', 'B', 'EASY'),
('LINE_CALL', 'If a player calls the ball out and then realizes it was in, what should they do?', 'The call stands', 'They should change the call to in, losing the rally', 'Ask the referee', 'Replay the point', 'B', 'HARD'),
('LINE_CALL', 'On a serve, the ball lands on the centerline of the service area. Is it in?', 'Yes', 'No', 'Only in doubles', 'It is a let', 'A', 'EASY'),
('LINE_CALL', 'If there is no referee and both teams cannot agree on a line call, what happens?', 'The serving team wins the point', 'The point is replayed', 'The ball is called out', 'The game is paused', 'B', 'HARD');

-- ==================== SERVE (10 questions) ====================
INSERT INTO referee_test_questions (category, question_text, option_a, option_b, option_c, option_d, correct_answer, difficulty) VALUES
('SERVE', 'In Pickleball, must the serve be underhand?', 'Yes', 'No, overhand is allowed', 'Only in singles', 'Only in tournament play', 'A', 'EASY'),
('SERVE', 'Where must the serve land?', 'Anywhere on the opponent''s side', 'In the diagonal service area, past the NVZ', 'In the kitchen', 'On the baseline only', 'B', 'EASY'),
('SERVE', 'At the point of contact, the paddle must be below what part of the body?', 'The shoulder', 'The waist (navel)', 'The knee', 'The hip', 'B', 'MEDIUM'),
('SERVE', 'Must both feet be behind the baseline when serving?', 'Yes, at the moment of contact', 'No, one foot can be on the line', 'Only the back foot', 'Only in doubles', 'A', 'MEDIUM'),
('SERVE', 'In doubles, which player serves first at the start of the game?', 'The player on the left', 'The player on the right', 'Either player can be chosen', 'The player with the higher ranking', 'B', 'MEDIUM'),
('SERVE', 'What is a "drop serve" in Pickleball?', 'A serve where the ball is dropped and hit after bouncing', 'A soft serve aimed at the kitchen', 'A serve that drops below the net', 'A serve with backspin', 'A', 'MEDIUM'),
('SERVE', 'If a player serves out of turn in doubles, what happens?', 'A fault is called', 'The point stands if not caught before the return', 'The serve is replayed', 'The team loses the game', 'A', 'HARD'),
('SERVE', 'Can the server bounce the ball before the serve (drop serve)?', 'Yes, it is allowed under current rules', 'No, it is always a fault', 'Only in casual play', 'Only with referee permission', 'A', 'MEDIUM'),
('SERVE', 'After the serve, what rule must both teams follow before volleying?', 'No rule, volley anytime', 'The two-bounce rule (each side must let the ball bounce once)', 'Wait 3 seconds', 'Only the receiving team must let it bounce', 'B', 'EASY'),
('SERVE', 'If the serve hits the net post and lands in the correct service area, what is the call?', 'Fault', 'Let, replay', 'The serve is good', 'Side-out', 'A', 'HARD');

-- ==================== NVZ / Kitchen (10 questions) ====================
INSERT INTO referee_test_questions (category, question_text, option_a, option_b, option_c, option_d, correct_answer, difficulty) VALUES
('NVZ', 'What is the Non-Volley Zone (NVZ) commonly called?', 'The Danger Zone', 'The Kitchen', 'The Penalty Box', 'The Service Zone', 'B', 'EASY'),
('NVZ', 'How far does the NVZ extend from the net on each side?', '5 feet', '7 feet', '10 feet', '3 feet', 'B', 'EASY'),
('NVZ', 'Can a player stand in the NVZ at any time?', 'Yes, but they cannot volley while in it', 'No, players must never enter the NVZ', 'Only during the serve', 'Only if the ball bounces first', 'A', 'MEDIUM'),
('NVZ', 'If a player volleys a ball while their foot is on the NVZ line, is it a fault?', 'Yes', 'No', 'Only in doubles', 'Only during the serve', 'A', 'MEDIUM'),
('NVZ', 'After hitting a volley, a player''s hat falls into the NVZ. Is it a fault?', 'Yes, any item touching the NVZ after a volley is a fault', 'No, only body contact counts', 'Only if the hat touches the net', 'Only in tournament play', 'A', 'HARD'),
('NVZ', 'Can a player hit a ball while standing in the NVZ if the ball has bounced?', 'Yes, as long as the ball bounced first', 'No, never hit from the NVZ', 'Only in singles', 'Only if they jump out after hitting', 'A', 'MEDIUM'),
('NVZ', 'A player jumps from outside the NVZ, volleys the ball in the air, and lands in the NVZ. Is it a fault?', 'Yes', 'No, because they were airborne', 'Only if they land before the ball crosses the net', 'Only in doubles', 'A', 'HARD'),
('NVZ', 'Can the serve land in the Non-Volley Zone?', 'No, it is a fault', 'Yes, it is allowed', 'Only if it hits the NVZ line', 'Only in singles', 'A', 'EASY'),
('NVZ', 'If a player enters the NVZ to play a bounced ball, do they need to exit before the next shot?', 'They should exit but it''s not required', 'Yes, they must exit before volleying', 'No, they can stay', 'Only in tournament play', 'B', 'MEDIUM'),
('NVZ', 'What is the purpose of the Non-Volley Zone rule?', 'To prevent smashing at the net', 'To make the game easier', 'To give advantage to the serving team', 'To limit court movement', 'A', 'EASY');
