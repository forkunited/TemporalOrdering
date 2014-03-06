import make_feat_dataset,runningSVM,sys,numpy

#input: a list of [features, label], a map from feature to number, a map from label to number

def argmax_label_score(scores):
	highest = scores[0]
	l = 0
	for i in scores.keys():
		if scores[i] > highest:
			highest = scores[i]
			l = i
	return l

def compute_scores(weights, feat_vect, label):
	return numpy.dot(weights[len(feat_vect)*label : len(feat_vect) * (label+1)], feat_vect)

def add_alpha_to_weights(weights, label, feats, alpha):
	weights[len(feat_vect)*label : len(feat_vect) * (label + 1)] += alpha * feats

def perceptron_learn(weights, feat_vects, labels):
	#learning rate:
	learn_rate = 1
	#to loop over the examples
	for i in range(len(labels)):
		scores = {}
		#to loop over the possible predicted labels
		for pred_l in range(max(labels) + 1):
			scores[pred_l] = compute_scores(weights, feat_vects[i], pred_l)
		highest = argmax_label_score(scores)
		if (labels[i] != highest):
			for j in range(max(labels) + 1):
				if (j == labels[i]):
					add_alpha_to_weights(weights, labels[i], feat_vects[i], learn_rate)
				elif (scores[j] > scores[labels[i]]):
					add_alpha_to_weights(weights, labels[i], feat_vects[i], -learn_rate)
		#print 'the example: '
		#print [labels[i], feat_vects[i]]
		#print 'the new weights: '
		#print weights
		#sys.exit(0)

def predict(weights, feat_vects, num_labels):
	pred_labels = []
	for i in range(len(feat_vects)):
		scores = {}
		#to loop over the possible predicted labels
		for pred_l in range(num_labels + 1):
			scores[pred_l] = compute_scores(weights, feat_vects[i], pred_l)
		pred_labels.append(argmax_label_score(scores))
	return pred_labels

def compare(pred, true):
	same = []
	for i in range(len(pred)):
		if pred[i] == true[i]:
			same.append(1)
		else:
			same.append(0)
	return same

def get_weights():
	print "making training data..."
	features = make_feat_dataset.full_dataset()
	print "done!"
	print "making features..."
	#to make indicator features for each feature and label:
	feat_nums = {}
	label_nums = {}
	examples = runningSVM.make_feat_nums(features, feat_nums, label_nums)
	
	[labels, feat_vects] = runningSVM.make_feat_vect(examples, feat_nums, label_nums)
	weights = [0] * (len(feat_nums) * len(label_nums))
	perceptron_learn(weights, feat_vects, labels)
	return weights

def main():

	pred_labels = predict(weights, feat_vects, max(labels))



if __name__ == '__main__':
	main()

