import make_feat_dataset,sys
from svmutil import *

def make_feat_nums(paths, feat_nums, label_nums):
	examples = []
	for doc in paths.keys():
		for sent in paths[doc].keys():
			for feat_type in paths[doc][sent].keys():
				for example in paths[doc][sent][feat_type].keys():
					path = paths[doc][sent][feat_type][example][0]
					label = paths[doc][sent][feat_type][example][1]
					if path not in feat_nums.keys():
						feat_nums[path] = len(feat_nums.keys())
					if label not in label_nums.keys():
						label_nums[label] = len(label_nums.keys())
					examples.append(paths[doc][sent][feat_type][example])
					
	return examples
	

def make_feat_vect(examples, feat_nums, label_nums):
	feat_vects = []
	label_vect = []
	for i in range(len(examples)):
		feats = [0] * len(feat_nums.keys())
		feats[feat_nums[examples[i][0]]] = 1
		l = label_nums[examples[i][1]]
		feat_vects.append(feats)
		label_vect.append(l)
	return [label_vect, feat_vects]

def train_svm(y, x):
	svm_model.predict = lambda self, x: svm_predict([0], [x], self)[0][0]
	prob = svm_problem(y, x)
	param = svm_parameter()
	param.kernel_type = LINEAR
	param.C = 10
	return svm_train(prob, param)

def find_train_error(m, label, feat_vects):
	y = []
	for e in feat_vects:
		y.append(m.predict(e))
	total_neg = 0
	total_pos = 0
	true_positive = 0
	true_negative = 0
	for i in range(len(y)):
		if y[i] == label[i] and label[i] == 0:
			true_negative += 1
		elif y[i] == label[i] and not label[i] == 0:
			true_positive += 1
		if y[i] == 0:
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

def main():
	print "making training data..."
	features = make_feat_dataset.full_dataset()
	print "done!"
	print "making features..."
	#to make indicator features for each feature and label:
	feat_nums = {}
	label_nums = {}
	examples = make_feat_nums(features, feat_nums, label_nums)

	[label, feat_vects] = make_feat_vect(examples, feat_nums, label_nums)
	print "done!"
	print "training svm..."
	m = train_svm(label, feat_vects)
	print "done!"
	find_train_error(m, label, feat_vects)

if __name__ == '__main__':
	main()
#print m.predict(positive_feats)


#print len(feat_nums.keys())
#print len(positive)
#print len(positive[0])

#to train the SVM: just use a unique number for each output label



