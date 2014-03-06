import sys

def generate(gold, pred):
	confusion = [[0 for x in range(max(gold) + 1)] for x in range(max(gold) + 1)]
	for i in range(len(gold)):
		confusion[gold[i]][pred[i]] += 1
	for i in range(len(confusion)):
		for j in range(len(confusion)):
			print '\t' + `confusion[i][j]`,
		print

def f_one(gold, pred):
	total_neg = 0
	total_pos = 0
	true_positive = 0
	true_negative = 0
	for i in range(len(gold)):
		if pred[i] == gold[i] and gold[i] == 0:
			true_negative += 1
		elif pred[i] == gold[i] and not gold[i] == 0:
			true_positive += 1
		if pred[i] == 0:
			total_neg += 1
		else:
			total_pos += 1
	false_negative = total_pos - true_positive
	false_positive = total_neg - true_negative
	precision = 1.0 * true_positive / (true_positive + false_positive)
	recall = 1.0 * true_positive / (true_positive + false_negative)
	f_one = 2*(precision * recall)/(precision + recall)
	
	print 'total with label: ', total_pos
	print 'total without label: ', total_neg
	print 'precision: ', precision
	print 'recall: ', recall
	print 'f1: ', f_one
