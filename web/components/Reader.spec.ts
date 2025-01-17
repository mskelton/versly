import { Node } from '@/lib/types/usfm'

export const TEST_PASSAGE: Node[] = [
	['cl', 'Matthew', 5],
	['ms', 'The Sermon on the Mount'],
	[
		'm',
		[
			['v', 1],
			['t', 'When he saw the crowds, he went up on the mountain,  '],
			['t', ' and after he sat down, his disciples came to him. '],
		],
	],
	['s', 1, 'The Beatitudes'],
	[
		'qc',
		[
			['t', '  '],
			['v', 3],
			['t', '  '],
			['wj', '“Blessed are the poor in spirit,'],
			['t', '   '],
		],
	],
	['cl', 'Psalm', 3],
	['s', 1, 'Confidence in Troubled Times'],
	['d', 'A psalm of David when he fled from his son Absalom.  '],
	[
		'q',
		1,
		[
			['v', 1],
			['nd', 'Lord'],
			['t', ', how my foes increase!'],
		],
	],
	['q', 1, [['t', 'There are many who attack me.  ']]],
	[
		'q',
		1,
		[
			['t', '  '],
			['v', 2],
			['t', 'Many say about me,'],
		],
	],
	[
		'q',
		1,
		[
			['t', '“There is no help for him in God.”  '],
			['t', '  '],
			['qs', 'Selah'],
		],
	],
	['cl', 'Job', 3],
	['s', 1, "Job's Complaint to God"],
	[
		'p',
		[
			['t', '  '],
			['v', 1],
			['t', '  '],
			[
				't',
				' Finally Job broke the silence and cursed the day on which he had been born.',
			],
		],
	],
	['sp', 'Job'],
	[
		'q',
		1,
		[
			['t', '  '],
			['v', 2],
			['t', '  '],
			['t', ' O God, put a curse on the day I was born;'],
		],
	],
	['cl', 'Mark', 16],
	[
		'iex',
		'[The earliest manuscripts and some other ancient witnesses do not have verses 9–20.]',
	],
	[
		'p',
		[
			['t', '  '],
			['v', 9],
			['t', '  '],
			[
				'it',
				'When Jesus rose early on the first day of the week, he appeared first to Mary Magdalene, out of whom he had driven seven demons.',
			],
			['t', '  '],
			['v', 10],
			[
				'it',
				'She went and told those who had been with him and who were mourning and weeping.',
			],
			['t', '  '],
			['v', 11],
			[
				'it',
				'When they heard that Jesus was alive and that she had seen him, they did not believe it.',
			],
		],
	],
	['cl', 'Judges', 11],
	[
		'pmo',
		[
			[
				't',
				'“This is what Jephthah says: Israel did not take the land of Moab or the land of the Ammonites. ',
			],
			['v', 16],
			[
				't',
				'But when they came up out of Egypt, Israel went through the wilderness to the Red Sea',
			],
			['t', ' and on to Kadesh. '],
			['v', 17],
			[
				't',
				'Then Israel sent messengers to the king of Edom, saying, ‘Give us permission to go through your country,’ but the king of Edom would not listen. They sent also to the king of Moab, and he refused. So Israel stayed at Kadesh.',
			],
		],
	],
	[
		'pm',
		[
			['t', '  '],
			['v', 18],
			[
				't',
				'“Next they traveled through the wilderness, skirted the lands of Edom and Moab, passed along the eastern side of the country of Moab, and camped on the other side of the Arnon. They did not enter the territory of Moab, for the Arnon was its border.',
			],
		],
	],
	['pmr', [['t', 'Then all the people shall say, “Amen!”']]],
	[
		'pm',
		[
			['t', '  '],
			['v', 28],
			[
				't',
				'The king of Ammon, however, paid no attention to the message Jephthah sent him.',
			],
		],
	],
	[
		'pm',
		[
			['t', '  '],
			['v', 28],
			[
				't',
				'The king of Ammon, however, paid no attention to the message Jephthah sent him.',
			],
		],
	],
	['pmc', [['t', 'Farewell.']]],
	['cl', 'Ezekiel', 48],
	[
		'p',
		[
			['t', '  '],
			['v', 35],
			['t', '“The distance all around will be 18,000 cubits.'],
		],
	],
	['p', [['t', '“And the name of the city from that time on will be:']]],
	[
		'pc',
		[
			['t', 'the '],
			['nd', 'Lord'],
			['t', ' is there.”'],
		],
	],
	['cl', 'Isaiah', 14],
	['q', 1, [['t', 'They will never rise up to possess a land']]],
	['q', 1, [['t', 'or fill the surface of the earth with cities.']]],
	['b'],
	[
		'p',
		[
			['t', '  '],
			['v', 22],
			[
				't',
				'“I will rise up against them” #— #this is the declaration of the  ',
			],
			['nd', 'Lord'],
			[
				't',
				' of Armies #— #“and I will cut off from Babylon her reputation, remnant, offspring, and posterity” #— #this is the ',
			],
			['nd', 'Lord'],
			['t', '’s declaration. '],
			['v', 23],
			['t', '“I will make her a swampland and a region for herons,  '],
			['t', ' and I will sweep her away with the broom of destruction.”'],
		],
	],
	[
		'pr',
		[
			['t', 'This is the declaration of the  '],
			['nd', 'Lord'],
			['t', ' of Armies.'],
		],
	],
	['cl', 'Psalm', 136],
	[
		'q',
		1,
		[
			['t', '  '],
			['v', 1],
			['t', 'Give thanks to the '],
			['nd', 'Lord'],
			['t', ', for he is good.'],
		],
	],
	['qr', [['t', 'His love endures forever.']]],
	[
		'q',
		1,
		[
			['t', '  '],
			['v', 2],
			['t', 'Give thanks to the God of gods.'],
		],
	],
	['qr', [['t', 'His love endures forever.']]],
	['cl', '1 Chronicles', 25],
	[
		'table',
		[
			[
				['th', [['t', 'Tribe']]],
				['th', [['t', 'Leader']]],
			],
			[
				['td', [['t', 'Reuben']]],
				['td', [['t', 'Elizur son of Shedeur']]],
			],
			[
				[
					'td',
					[
						['v', 6],
						['t', 'Simeon'],
					],
				],
				['td', [['t', 'Shelumiel son of Zurishaddai']]],
			],
			[
				[
					'td',
					[
						['v', 7],
						['t', 'Judah'],
					],
				],
				['td', [['t', 'Nahshon son of Amminadab']]],
			],
			[
				[
					'td',
					[
						['v', 8],
						['t', 'Issachar'],
					],
				],
				['td', [['t', 'Nethanel son of Zuar']]],
			],
			[
				[
					'td',
					[
						['v', 9],
						['t', 'Zebulun'],
					],
				],
				['td', [['t', 'Eliab son of Helon']]],
			],
			[
				[
					'td',
					[
						['v', 10],
						['t', 'Ephraim son of Joseph'],
					],
				],
				['td', [['t', 'Elishama son of Ammihud']]],
			],
			[
				['td', [['t', 'Manasseh son of Joseph']]],
				['td', [['t', 'Gamaliel son of Pedahzur']]],
			],
			[
				[
					'td',
					[
						['v', 11],
						['t', 'Benjamin'],
					],
				],
				['td', [['t', 'Abidan son of Gideoni']]],
			],
			[
				[
					'td',
					[
						['v', 12],
						['t', 'Dan'],
					],
				],
				['td', [['t', 'Ahiezer son of Ammishaddai']]],
			],
			[
				[
					'td',
					[
						['v', 13],
						['t', 'Asher'],
					],
				],
				['td', [['t', 'Pagiel son of Ocran']]],
			],
			[
				[
					'td',
					[
						['v', 14],
						['t', 'Gad'],
					],
				],
				['td', [['t', 'Eliasaph son of Deuel']]],
			],
			[
				[
					'td',
					[
						['v', 15],
						['t', 'Naphtali'],
					],
				],
				['td', [['t', 'Ahira son of Enan']]],
			],
		],
	],
	['cl', 'Joshua', 12],
	['s', 1, 'Territory West of the Jordan'],
	[
		'm',
		[
			['t', '  '],
			['v', 7],
			[
				't',
				'Joshua and the Israelites struck down the following kings of the land beyond the Jordan to the west, from Baal-gad in the Valley of Lebanon to Mount Halak,  ',
			],
			[
				't',
				' which ascends toward Seir (Joshua gave their land as an inheritance to the tribes of Israel according to their allotments: ',
			],
			['v', 8],
			['t', 'the hill country, the Judean foothills,  '],
			[
				't',
				' the Arabah, the slopes, the wilderness, and the Negev #— #the lands of the Hethites, Amorites, Canaanites, Perizzites, Hivites, and Jebusites):',
			],
		],
	],
	[
		'lim',
		1,
		[
			['t', '  '],
			['v', 9],
			['t', 'the king of Jericho  '],
			['t', '  '],
			['litl', 'one'],
		],
	],
	[
		'lim',
		1,
		[
			['t', 'the king of Ai,  '],
			['t', ' which is next to Bethel '],
			['litl', 'one'],
		],
	],
	['cl', '1 Chronicles', 15],
	[
		'p',
		[
			[
				't',
				'Zechariah, Aziel, Shemiramoth, Jehiel, Unni, Eliab, Maaseiah, and Benaiah were to play harps according to ',
			],
			['em', 'Alamoth'],
			['t', '   '],
			['t', '  '],
			['v', 21],
			[
				't',
				'and Mattithiah, Eliphelehu, Mikneiah, Obed-edom, Jeiel, and Azaziah were to lead the music with lyres according to the ',
			],
			['em', 'Sheminith'],
			['t', '. '],
		],
	],
	['cl', 'Genesis', 1],
	[
		'm',
		[
			['t', '  '],
			['v', 1],
			['t', 'In the beginning  '],
			['t', ' God created the heavens and the earth.  '],
			['sup', ','],
		],
	],
]
