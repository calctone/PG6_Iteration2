//
//  DetailViewController.h
//  PG6_NightBot_Romo
//
//  Created by Jeff Lanning on 7/12/15.
//  Copyright (c) 2015 Jeff Lanning. All rights reserved.
//

#import <UIKit/UIKit.h>

#import <AVFoundation/AVFoundation.h>
#import <opencv2/imgproc/imgproc_c.h>
#import <opencv2/opencv.hpp>
#import "opencv2/nonfree/nonfree.hpp"
#import <opencv2/highgui/cap_ios.h>
#include <math.h>

#import <RMCore/RMCore.h>
#import <RMCharacter/RMCharacter.h>

#include <ifaddrs.h>
#include <arpa/inet.h>

@interface DetailViewController : UIViewController<AVCaptureVideoDataOutputSampleBufferDelegate, RMCoreDelegate> {
    
    AVCaptureSession *_session;
    AVCaptureDevice *_captureDevice;
    
    BOOL _useBackCamera;
}

@property (strong, nonatomic) IBOutlet UIImageView *imageView;
@property (nonatomic, strong) RMCoreRobotRomo3 *Romo3;
@property (nonatomic, strong) RMCharacter *Romo;
@property (strong, nonatomic) IBOutlet UIView *romoView;
@property (strong, nonatomic) id detailItem;
@property (weak, nonatomic) IBOutlet UILabel *detailDescriptionLabel;

- (void)addGestureRecognizers;

- (UIImage*)getUIImageFromIplImage:(IplImage *)iplImage;
- (void)didCaptureIplImage:(IplImage *)iplImage :(NSString *)triggerImageURL;
- (void)didFinishProcessingImage:(IplImage *)iplImage;
- (void)turnCameraOn;
- (void) turnCameraOff;

@end

