package temp.data.annotation.nlp;

// From Penn Treebank (see http://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html)
// FreeLing mapped from http://nlp.lsi.upc.edu/freeling/doc/tagsets/tagset-es.html
public enum PoSTag { 
	CC, // Coordinating conjunction                 -- (Freeling: CC)
	CD, // Cardinal number                          -- (Freeling: Z.)
	DT, // Determiner                               -- (Freeling: D.....)
	EX, // Existential there                        -- (Freeling: None)
	FW, // Foreign word                             -- (Freeling: None)
	IN, // Preposition or subordinating conjunction -- (Freeling: CS S....)
	JJ, // Adjective                                -- (Freeling: A.....)
	JJR, // Adjective, comparative                  -- (Freeling: None)
	JJS, // Adjective, superlative                  -- (Freeling: A.S...)
	LS, // List item marker                         -- (Freeling: None)
	MD, // Modal                                    -- (Freeling: None)
	NN, // Noun, singular or mass                   -- (Freeling: N......)
	NNS, // Noun, plural                            -- (Freeling: N..P...)
	NNP, // Proper noun, singular                   -- (Freeling: NP.S...)
	NNPS, // Proper noun, plural                    -- (Freeling: NP.P...)
	PDT, // Predeterminer                           -- (Freeling: None)
	POS, // Possessive ending                       -- (Freeling: None)
	PRP, // Personal pronoun                        -- (Freeling: PP......)
	PRP$, // Possessive pronoun                     -- (Freeling: PX......)
	RB, // Adverb                                   -- (Freeling: R.)
	RBR, // Adverb, comparative                     -- (Freeling: None)
	RBS, // Adverb, superlative                     -- (Freeling: None)
	RP, // Particle                                 -- (Freeling: None)
	SYM, // Symbol                                  -- (Freeling: F.*)
	TO, // to                                       -- (Freeling: None)
	UH, // Interjection                             -- (Freeling: PE...... I)
	VB, // Verb, base form                          -- (Freeling: V......)
	VBD, // Verb, past tense                        -- (Freeling: V..I... V..S...)
	VBG, // Verb, gerund or present participle      -- (Freeling: V.G.... V.PP...)
	VBN, // Verb, past participle                   -- (Freeling: V.PI... V.PS...)
	VBP, // Verb, non�3rd person singular present   -- (Freeling: V..P.S.)
	VBZ, // Verb, 3rd person singular present       -- (Freeling: V..P3S.)
	WDT, // Wh�determiner                           -- (Freeling: None)
	WP, // Wh�pronoun                               -- (Freeling: PT...... PR......)
	WP$, // Possessive wh�pronoun                   -- (Freeling: None)
	WRB, // Wh�adverb                               -- (Freeling: None)
	Other // Unknown								-- (Frelinng: Everything else
}
